package dev.rmaiun.soos.services

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.{Flow, MonadThrowable}
import dev.rmaiun.soos.db.entities.{User, UserRealmRole}
import dev.rmaiun.soos.errors.UserErrors.UserNotFoundException
import dev.rmaiun.soos.repositories.UserRepo
import doobie.hikari.HikariTransactor
import doobie.implicits._

trait UserService[F[_]] {
  def assignToRealm(realmId: Long, userId: Long, roleId: Long, botUsage: Boolean = false): Flow[F, Int]
  def findByInputType(surname: Option[String] = None, tid: Option[Long] = None): Flow[F, User]
  def list(realm: String, activeStatus: Option[Boolean] = None): Flow[F, List[User]]
  def checkAllPresent(realm: String, users: List[String]): Flow[F, Unit]
  def update(u: User): Flow[F, User]
  def create(u: User): Flow[F, User]
  def findAvailableId: Flow[F, Long]
}

object UserService {
  def apply[F[_]](implicit ev: UserService[F]): UserService[F] = ev
  def impl[F[_]: MonadThrowable](
    xa: HikariTransactor[F],
    userRepo: UserRepo[F]
  ): UserService[F] = new UserService[F] {

    override def create(u: User): Flow[F, User] =
      userRepo.create(u).transact(xa).attemptSql.adaptError

    override def list(realm: String, activeStatus: Option[Boolean]): Flow[F, List[User]] =
      userRepo.listAll(realm, active = activeStatus).transact(xa).attemptSql.adaptError

    override def findByInputType(surname: Option[String] = None, tid: Option[Long] = None): Flow[F, User] =
      (tid, surname) match {
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

    def update(u: User): Flow[F, User] =
      userRepo.update(u).transact(xa).attemptSql.adaptError

    def assignToRealm(realmId: Long, userId: Long, roleId: Long, botUsage: Boolean = false): Flow[F, Int] =
      userRepo.assignUserToRealm(UserRealmRole(realmId, userId, roleId, botUsage)).transact(xa).attemptSql.adaptError

    def checkAllPresent(realm: String, users: List[String]): Flow[F, Unit] =
      list(realm).flatMap { fromDb =>
        val found   = fromDb.map(_.surname)
        val useless = users.filter(u => !found.contains(u))
        if (useless.isEmpty) {
          Flow.unit
        } else {
          Flow.error(UserNotFoundException(Map("wrongUsers" -> useless.mkString(","))))
        }
      }

    override def findAvailableId: Flow[F, Long] =
      userRepo.findAvailableId.transact(xa).attemptSql.adaptError
  }
}
