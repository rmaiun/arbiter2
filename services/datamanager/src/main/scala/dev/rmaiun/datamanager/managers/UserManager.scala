package dev.rmaiun.datamanager.managers

import cats.Monad
import cats.implicits._
import dev.rmaiun.datamanager.db.entities.User
import dev.rmaiun.datamanager.dtos.api.UserDtos._
import dev.rmaiun.datamanager.helpers.DtoMapper.{realmToDto, userToDto}
import dev.rmaiun.datamanager.services.{RealmService, RoleService, UserRightsService, UserService}
import dev.rmaiun.datamanager.validations.UserValidationSet._
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.validation.Validator

import java.time.{ZoneOffset, ZonedDateTime}

trait UserManager[F[_]] {
  def findUser(dtoIn: FindUserDtoIn): Flow[F, FindUserDtoOut]
  def findAllUsers(dtoIn: FindAllUsersDtoIn): Flow[F, FindAllUsersDtoOut]
  def assignUserToRealm(dtoIn: AssignUserToRealmDtoIn): Flow[F, AssignUserToRealmDtoOut]
  def switchActiveRealm(dtoIn: SwitchActiveRealmDtoIn): Flow[F, SwitchActiveRealmDtoOut]
  def processActivation(dtoIn: ProcessActivationDtoIn): Flow[F, ProcessActivationDtoOut]
  def linkTid(dtoIn: LinkTidDtoIn): Flow[F, LinkTidDtoOut]
  def findRelatedRealms(dtoIn: FindAvailableRealmsDtoIn): Flow[F, FindAvailableRealmsDtoOut]
}
object UserManager {
  def apply[F[_]](implicit ev: UserManager[F]): UserManager[F] = ev

  def impl[F[_]: Monad](
    userService: UserService[F],
    userRightsService: UserRightsService[F],
    realmService: RealmService[F],
    roleService: RoleService[F]
  ): UserManager[F] = new UserManager[F] {
    override def findUser(dtoIn: FindUserDtoIn): Flow[F, FindUserDtoOut] =
      for {
        _ <- Validator.validateDto[F, FindUserDtoIn](dtoIn)
        u <- userService.findByInputType(dtoIn.surname, dtoIn.tid)
      } yield FindUserDtoOut(userToDto(u))

    override def findAllUsers(dtoIn: FindAllUsersDtoIn): Flow[F, FindAllUsersDtoOut] =
      for {
        _     <- Validator.validateDto[F, FindAllUsersDtoIn](dtoIn)
        users <- userService.list(dtoIn.realm, dtoIn.activeStatus)
      } yield FindAllUsersDtoOut(users.map(userToDto))

    override def assignUserToRealm(dtoIn: AssignUserToRealmDtoIn): Flow[F, AssignUserToRealmDtoOut] = {
      val result = for {
        _        <- Validator.validateDto[F, AssignUserToRealmDtoIn](dtoIn)
        user     <- userService.findByInputType(Some(dtoIn.user))
        realm    <- realmService.getByName(dtoIn.realm)
        roleValue = dtoIn.role.getOrElse("Executive")
        role     <- roleService.findRoleByName(roleValue)
        _        <- userService.assignToRealm(realm.id, user.id, role.id)
      } yield AssignUserToRealmDtoOut(
        user.surname,
        realm.name,
        role.value,
        switchedAsActive = dtoIn.switchAsActive
      )
      val activationFlag = dtoIn.switchAsActive.getOrElse(false)
      if (activationFlag) {
        for {
          r <- result
          _ <- switchActiveRealm(SwitchActiveRealmDtoIn(r.user, r.realm))
        } yield r
      } else {
        result
      }
    }

    override def switchActiveRealm(dtoIn: SwitchActiveRealmDtoIn): Flow[F, SwitchActiveRealmDtoOut] =
      for {
        _     <- Validator.validateDto[F, SwitchActiveRealmDtoIn](dtoIn)
        user  <- userService.findByInputType(Some(dtoIn.user))
        realm <- realmService.getByName(dtoIn.realm)
        role  <- roleService.findUserRoleByRealm(user.surname, realm.name)
        _     <- userService.assignToRealm(realm.id, user.id, role.id, botUsage = true)
      } yield SwitchActiveRealmDtoOut(realm.name)

    override def processActivation(dtoIn: ProcessActivationDtoIn): Flow[F, ProcessActivationDtoOut] =
      for {
        _ <- Validator.validateDto[F, ProcessActivationDtoIn](dtoIn)
        _ <- userRightsService.checkUserWritePermissions(dtoIn.realm, dtoIn.moderatorTid)
        _ <- userService.checkAllPresent(dtoIn.realm, dtoIn.users)
        _ <- processUsersActivations(dtoIn.users, dtoIn.activate)

      } yield ProcessActivationDtoOut(dtoIn.users, dtoIn.activate)

    override def linkTid(dtoIn: LinkTidDtoIn): Flow[F, LinkTidDtoOut] =
      for {
        _    <- Validator.validateDto[F, LinkTidDtoIn](dtoIn)
        _    <- userRightsService.checkUserWritePermissions(dtoIn.realm, dtoIn.moderatorTid)
        user <- userService.findByInputType(surname = Some(dtoIn.nameToLink))
        upd  <- userService.update(user.copy(tid = Some(dtoIn.tid)))
      } yield LinkTidDtoOut(upd.surname, dtoIn.tid, ZonedDateTime.now(ZoneOffset.UTC))

    override def findRelatedRealms(dtoIn: FindAvailableRealmsDtoIn): Flow[F, FindAvailableRealmsDtoOut] =
      for {
        _      <- Validator.validateDto[F, FindAvailableRealmsDtoIn](dtoIn)
        realms <- realmService.findByUser(dtoIn.surname)
      } yield FindAvailableRealmsDtoOut(realms.map(realmToDto(_)))

    private def processUsersActivations(users: List[String], activationState: Boolean): Flow[F, List[User]] =
      users.map { u =>
        for {
          u   <- userService.findByInputType(surname = Some(u))
          upd <- userService.update(u.copy(active = activationState))
        } yield upd
      }.sequence
  }
}
