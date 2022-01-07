package dev.rmaiun.soos.services

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.soos.db.entities.Role
import dev.rmaiun.soos.db.projections.UserRole
import dev.rmaiun.soos.errors.RoleErrors.RoleNotFoundRuntimeException
import dev.rmaiun.soos.repositories.RoleRepo
import doobie.hikari.HikariTransactor
import doobie.implicits._

trait RoleService[F[_]] {
  def saveRole(value: String, permission: Int): Flow[F, Role]
  def findRoleByName(value: String): Flow[F, Role]
  def findUserRoleByRealm(surname: String, realm: String): Flow[F, Role]
  def findUserRoleByRealm(userTid: Long, realm: String): Flow[F, Role]
  def findAllUserRolesForRealm(realm: String): Flow[F, List[UserRole]]
  def findAllRoles: Flow[F, List[Role]]
}

object RoleService {
  def apply[F[_]](implicit ev: RoleService[F]): RoleService[F] = ev

  def impl[F[_]: Monad: Sync](xa: HikariTransactor[F], roleRepo: RoleRepo[F]): RoleService[F] =
    new RoleService[F] {
      override def saveRole(value: String, permission: Int): Flow[F, Role] =
        roleRepo.create(Role(0, value, permission)).transact(xa).attemptSql.adaptError

      override def findRoleByName(value: String): Flow[F, Role] =
        roleRepo.getByValue(value).transact(xa).attemptSql.adaptError.flatMap {
          case Some(r) => Flow.pure(r)
          case None    => Flow.error(RoleNotFoundRuntimeException(Map("value" -> s"$value")))
        }

      override def findUserRoleByRealm(surname: String, realm: String): Flow[F, Role] =
        roleRepo.findUserRoleInRealm(surname, realm).transact(xa).attemptSql.adaptError.flatMap {
          case Some(r) => Flow.pure(r)
          case None    => Flow.error(RoleNotFoundRuntimeException(Map("surname" -> s"$surname", "realm" -> s"$realm")))
        }

      override def findUserRoleByRealm(userTid: Long, realm: String): Flow[F, Role] =
        roleRepo.findUserRoleInRealm(userTid, realm).transact(xa).attemptSql.adaptError.flatMap {
          case Some(r) => Flow.pure(r)
          case None    => Flow.error(RoleNotFoundRuntimeException(Map("userTid" -> s"$userTid", "realm" -> s"$realm")))
        }

      override def findAllUserRolesForRealm(realm: String): Flow[F, List[UserRole]] =
        roleRepo.findAllUserRolesForRealm(realm).transact(xa).attemptSql.adaptError

      override def findAllRoles: Flow[F, List[Role]] =
        roleRepo.listAll.transact(xa).attemptSql.adaptError
    }
}
