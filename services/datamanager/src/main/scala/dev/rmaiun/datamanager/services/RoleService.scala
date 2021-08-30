package dev.rmaiun.datamanager.services

import dev.rmaiun.datamanager.db.entities.Role
import dev.rmaiun.datamanager.errors.UserErrors.RoleNotFoundException
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow

trait RoleService[F[_]] {
  def saveRole(value: String, permission: Int): Flow[F, Role]
  def getRole(value: String): Flow[F, Role]
  def findRoleByName(value: String): Flow[F, Role]
  def findUserRoleByRealm(surname:String, realm:String): Flow[F, Role]
}

object RoleService{
//  roleRepo
//    .getByValue(role)
//    .transact(xa)
//    .attemptSql
//    .adaptError
//    .flatMap(x => Flow.fromOpt(x, RoleNotFoundException(Map("role" -> role))))
}
