package dev.rmaiun.datamanager.services

import cats.Monad
import dev.rmaiun.datamanager.db.entities.Role
import dev.rmaiun.datamanager.dtos.api.UserDtos.FindUserDtoIn
import dev.rmaiun.datamanager.errors.UserErrors.{ NoWritePermissionForUserFoundException, UserNotAuthorizedException }
import dev.rmaiun.datamanager.helpers.ConfigProvider.Config
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import io.chrisdavenport.log4cats.Logger

trait UserRightsService[F[_]] {
  def checkUserWritePermissions(realm: String, userSurname: String): Flow[F, Unit]
  def checkUserWritePermissions(realm: String, userTid: Long): Flow[F, Unit]
  def checkUserIsRegistered(tid: Long): Flow[F, Unit]
}

object UserRightsService {
  def apply[F[_]](implicit ev: UserRightsService[F]): UserRightsService[F] = ev
  def impl[F[_]: Monad: Logger](
    userService: UserService[F],
    roleService: RoleService[F]
  )(implicit cfg: Config): UserRightsService[F] = new UserRightsService[F] {
    override def checkUserWritePermissions(realm: String, user: String): Flow[F, Unit] =
      roleService.findUserRoleByRealm(user, realm).flatMap { role =>
        processWritePermissions(role, Map("user" -> s"$user", "realm" -> s"$realm"))
      }
    override def checkUserWritePermissions(realm: String, userTid: Long): Flow[F, Unit] =
      roleService.findUserRoleByRealm(userTid, realm).flatMap { role =>
        processWritePermissions(role, Map("userTid" -> s"$userTid", "realm" -> s"$realm"))
      }

    private def processWritePermissions(role: Role, errorMsg: Map[String, String]): Flow[F, Unit] =
      if (role.permission >= cfg.app.minWritePermission) {
        Flow.unit
      } else {
        Flow.error(NoWritePermissionForUserFoundException(errorMsg))
      }

    override def checkUserIsRegistered(tid: Long): Flow[F, Unit] =
      userService
        .findUser(FindUserDtoIn(tid = Some(tid)))
        .flatMap(u =>
          if (u.user.active) {
            Flow.unit
          } else {
            Flow.error(UserNotAuthorizedException(Map("tid" -> s"$tid")))
          }
        )
  }
}
