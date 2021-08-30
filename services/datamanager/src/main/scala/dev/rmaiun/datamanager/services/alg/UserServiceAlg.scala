package dev.rmaiun.datamanager.services.alg

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.datamanager.db.entities.{Role, User, UserRealmRole}
import dev.rmaiun.datamanager.dtos.api.RealmDtos.{GetRealmDtoIn, RealmDto}
import dev.rmaiun.datamanager.dtos.api.UserDtos._
import dev.rmaiun.datamanager.errors.UserErrors.UserNotFoundException
import dev.rmaiun.datamanager.helpers.ConfigProvider.AppConfig
import dev.rmaiun.datamanager.repositories.UserRepo
import dev.rmaiun.datamanager.services.{RealmService, RoleService, UserRightsService, UserService}
import dev.rmaiun.datamanager.validations.UserValidationSet._
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.validation.Validator
import doobie.hikari.HikariTransactor
import doobie.implicits._

class UserServiceAlg[F[_]: Monad: Sync](
  xa: HikariTransactor[F],
  userRepo: UserRepo[F],
  roleService: RoleService[F],
  realmService: RealmService[F],
  userRightsService: UserRightsService[F]
)(cfg:AppConfig) extends UserService[F] {
  override def findUser(dtoIn: FindUserDtoIn): Flow[F, FindUserDtoOut] =
    for {
      _ <- Validator.validateDto[F, FindUserDtoIn](dtoIn)
      u <- findUserByInputType(dtoIn)
    } yield FindUserDtoOut(userToDto(u))

  override def findAllUsers(dtoIn: FindAllUsersDtoIn): Flow[F, FindAllUsersDtoOut] =
    for {
      _     <- Validator.validateDto[F, FindAllUsersDtoIn](dtoIn)
      users <- userRepo.listAll(dtoIn.realm, active = dtoIn.activeStatus).transact(xa).attemptSql.adaptError
    } yield FindAllUsersDtoOut(users.map(userToDto))

  override def assignUserToRealm(dtoIn: AssignUserToRealmDtoIn): Flow[F, AssignUserToRealmDtoOut] = {
    val result = for {
      _        <- Validator.validateDto[F, AssignUserToRealmDtoIn](dtoIn)
      userDto  <- findUser(FindUserDtoIn(Some(dtoIn.user)))
      realmDto <- realmService.getRealm(GetRealmDtoIn(dtoIn.realm))
      roleValue = dtoIn.role.getOrElse("Executive")
      role     <- roleService.findRoleByName(roleValue)
      _        <- processAssignUserToRealm(userDto.user, role, realmDto.realm)
    } yield AssignUserToRealmDtoOut(
      userDto.user.surname,
      realmDto.realm.name,
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
      _        <- Validator.validateDto[F, SwitchActiveRealmDtoIn](dtoIn)
      userDto  <- findUser(FindUserDtoIn(Some(dtoIn.user)))
      realmDto <- realmService.getRealm(GetRealmDtoIn(dtoIn.realm))
      role     <- roleService.findUserRoleByRealm(userDto.user.surname, realmDto.realm.name)
      _        <- processAssignUserToRealm(userDto.user, role, realmDto.realm)
    } yield SwitchActiveRealmDtoOut(realmDto.realm.name)

  override def processActivation(dtoIn: ProcessActivationDtoIn): Flow[F, ProcessActivationDtoOut] = {
    for{
      _        <- Validator.validateDto[F, ProcessActivationDtoIn](dtoIn)
      user <- findUserByInputType(FindUserDtoIn(tid = Some(dtoIn.moderatorTid)))
      _ <- userRightsService.checkUserWritePermissions(dtoIn.realm, user.surname)

    }yield
  }

  override def linkTid(dtoIn: LinkTidDtoIn): Flow[F, LinkTidDtoOut] = ???

  override def changeSubscriptionStatus(dtoIn: ChangeSubscriptionStatusDtoIn): Flow[F, ChangeSubscriptionStatusDtoOut] =
    ???

  override def findRelatedRealms(dtoIn: FindAvailableRealmsDtoIn): Flow[F, FindAvailableRealmsDtoOut] = ???

  private def findUserByInputType(dtoIn: FindUserDtoIn): Flow[F, User] =
    (dtoIn.tid, dtoIn.surname) match {
      case (None, None) =>
        Flow.error(UserNotFoundException(Map("businessValidation" -> "Both tid and surname are not present")))
      case (Some(_), Some(_)) =>
        Flow.error(UserNotFoundException(Map("businessValidation" -> "Both tid and surname are present")))
      case (Some(tidV), None) =>
        userRepo
          .findByTid(tidV)
          .transact(xa)
          .attemptSql
          .adaptError
          .flatMap(u => Flow.fromOpt(u, UserNotFoundException(Map("tid" -> s"$tidV"))))
      case (None, Some(surnameV)) =>
        userRepo
          .findBySurname(surnameV)
          .transact(xa)
          .attemptSql
          .adaptError
          .flatMap(u => Flow.fromOpt(u, UserNotFoundException(Map("surname" -> s"$surnameV"))))
    }

  private def userToDto(u: User): UserDto = UserDto(u.id, u.surname, u.nickname, u.tid, u.active, u.createdAt)

  private def processAssignUserToRealm(user: UserDto, role: Role, realmDto: RealmDto): Flow[F, Int] =
    userRepo.assignUserToRealm(UserRealmRole(realmDto.id, user.id, role.id)).transact(xa).attemptSql.adaptError

  private def checkAllUsersPresent(users:List[String]) = {
    users.map(u => findUser(FindUserDtoIn(surname = Some(u))))
  }

  private def processUsersActivations(users:List[String], activate:Boolean) = {
    users.map(u => findUser(FindUserDtoIn(surname = Some(u))))
  }
}
