package dev.rmaiun.datamanager.services.alg

import cats.Monad
import cats.effect.Sync
import cats.implicits._
import dev.rmaiun.datamanager.db.entities.{ Role, User, UserRealmRole }
import dev.rmaiun.datamanager.dtos.api.RealmDtos.{ GetRealmDtoIn, RealmDto }
import dev.rmaiun.datamanager.dtos.api.UserDtos._
import dev.rmaiun.datamanager.errors.UserErrors.UserNotFoundException
import dev.rmaiun.datamanager.helpers.DtoMapper.{ realmToDto, userToDto }
import dev.rmaiun.datamanager.repositories.UserRepo
import dev.rmaiun.datamanager.services.{ RealmService, RoleService, UserRightsService, UserService }
import dev.rmaiun.datamanager.validations.UserValidationSet._
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.validation.Validator
import doobie.hikari.HikariTransactor
import doobie.implicits._

import java.time.{ ZoneOffset, ZonedDateTime }

class UserServiceImpl[F[_]: Monad: Sync](
  xa: HikariTransactor[F],
  userRepo: UserRepo[F],
  roleService: RoleService[F],
  realmService: RealmService[F],
  userRightsService: UserRightsService[F]
) extends UserService[F] {
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

  override def processActivation(dtoIn: ProcessActivationDtoIn): Flow[F, ProcessActivationDtoOut] =
    for {
      _ <- Validator.validateDto[F, ProcessActivationDtoIn](dtoIn)
      _ <- userRightsService.checkUserWritePermissions(dtoIn.realm, dtoIn.moderatorTid)
      _ <- checkAllUsersPresent(dtoIn.realm, dtoIn.users)
      _ <- processUsersActivations(dtoIn.users, dtoIn.activate)

    } yield ProcessActivationDtoOut(dtoIn.users, dtoIn.activate)

  override def linkTid(dtoIn: LinkTidDtoIn): Flow[F, LinkTidDtoOut] =
    for {
      _    <- Validator.validateDto[F, LinkTidDtoIn](dtoIn)
      _    <- userRightsService.checkUserWritePermissions(dtoIn.realm, dtoIn.moderatorTid)
      user <- findUserByInputType(FindUserDtoIn(surname = Some(dtoIn.nameToLink)))
      upd  <- processUserUpdate(user.copy(tid = Some(dtoIn.tid)))
    } yield LinkTidDtoOut(upd.surname, dtoIn.tid, ZonedDateTime.now(ZoneOffset.UTC))

  override def findRelatedRealms(dtoIn: FindAvailableRealmsDtoIn): Flow[F, FindAvailableRealmsDtoOut] =
    for {
      _      <- Validator.validateDto[F, FindAvailableRealmsDtoIn](dtoIn)
      realms <- realmService.findRealmsByUser(dtoIn.surname)
    } yield FindAvailableRealmsDtoOut(realms.map(realmToDto(_)))

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

  private def processUserUpdate(u: User): Flow[F, User] =
    userRepo.update(u).transact(xa).attemptSql.adaptError

  private def processAssignUserToRealm(user: UserDto, role: Role, realmDto: RealmDto): Flow[F, Int] =
    userRepo.assignUserToRealm(UserRealmRole(realmDto.id, user.id, role.id)).transact(xa).attemptSql.adaptError

  private def checkAllUsersPresent(realm: String, users: List[String]): Flow[F, Unit] =
    findAllUsers(FindAllUsersDtoIn(realm, None)).flatMap { dto =>
      val found   = dto.items.map(_.surname)
      val useless = users.filter(u => !found.contains(u))
      if (useless.isEmpty) {
        Flow.unit
      } else {
        Flow.error(UserNotFoundException(Map("wrongUsers" -> useless.mkString(","))))
      }
    }

  private def processUsersActivations(users: List[String], activate: Boolean): Flow[F, List[User]] =
    users.map { u =>
      for {
        u <- findUser(FindUserDtoIn(surname = Some(u)))
        upd <-
          processUserUpdate(User(u.user.id, u.user.surname, u.user.nickname, u.user.tid, activate, u.user.createdAt))
      } yield upd
    }.sequence
}
