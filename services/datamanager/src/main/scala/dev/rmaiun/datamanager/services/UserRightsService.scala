package dev.rmaiun.datamanager.services

import cats.Monad
import dev.rmaiun.datamanager.helpers.ConfigProvider.Config
import dev.rmaiun.flowtypes.Flow.Flow
import io.chrisdavenport.log4cats.Logger

trait UserRightsService[F[_]] {
  def checkUserWritePermissions(realm: String, user: String): Flow[F, Unit]
  def checkUserIsRegistered(tid: String): Flow[F, Unit]
}

object UserRightsService {
  def apply[F[_]](implicit ev: UserRightsService[F]): UserRightsService[F] = ev
  def impl[F[_]: Monad: Logger](
    userService: UserService[F]
  )(implicit cfg: Config): UserRightsService[F] = new UserRightsService[F] {
    override def checkUserWritePermissions(realm: String, user: String): Flow[F, Unit] = ???

    override def checkUserIsRegistered(tid: String): Flow[F, Unit] = ???
  }
}
