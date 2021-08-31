package dev.rmaiun.datamanager.services

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.datamanager.db.entities.UserRealmRole
import dev.rmaiun.datamanager.dtos.api.UserDtos._
import dev.rmaiun.datamanager.repositories.UserRepo
import dev.rmaiun.datamanager.services.alg.UserServiceImpl
import dev.rmaiun.flowtypes.Flow.Flow
import doobie.hikari.HikariTransactor
import io.chrisdavenport.log4cats.Logger

trait UserService[F[_]] {
  def findUser(dtoIn: FindUserDtoIn): Flow[F, FindUserDtoOut]
  def findAllUsers(dtoIn: FindAllUsersDtoIn): Flow[F, FindAllUsersDtoOut]
  def assignUserToRealm(dtoIn: AssignUserToRealmDtoIn): Flow[F, AssignUserToRealmDtoOut]
  def switchActiveRealm(dtoIn: SwitchActiveRealmDtoIn): Flow[F, SwitchActiveRealmDtoOut]
  def processActivation(dtoIn: ProcessActivationDtoIn): Flow[F, ProcessActivationDtoOut]
  def linkTid(dtoIn: LinkTidDtoIn): Flow[F, LinkTidDtoOut]
  def findRelatedRealms(dtoIn: FindAvailableRealmsDtoIn): Flow[F, FindAvailableRealmsDtoOut]
}

object UserService {
  def apply[F[_]](implicit ev: UserService[F]): UserService[F] = ev
  def impl[F[_]: Monad: Logger: Sync](
    xa: HikariTransactor[F],
    userRepo: UserRepo[F],
    roleService: RoleService[F],
    realmService: RealmService[F],
    userRightsService: UserRightsService[F]
  ): UserServiceImpl[F] = new UserServiceImpl[F](xa, userRepo, roleService, realmService, userRightsService)
}
