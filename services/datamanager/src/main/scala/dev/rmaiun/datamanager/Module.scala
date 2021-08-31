package dev.rmaiun.datamanager

import cats.Monad
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import dev.rmaiun.datamanager.helpers.{ConfigProvider, TransactorProvider}
import dev.rmaiun.datamanager.repositories.{AlgorithmRepo, RealmRepo, RoleRepo, UserRepo}
import dev.rmaiun.datamanager.routes.DataManagerRoutes
import dev.rmaiun.datamanager.services.{AlgorithmService, RealmService, RoleService, UserRightsService, UserService}
import io.chrisdavenport.log4cats.Logger
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.client.Client
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.implicits._

object Module {
  def initHttpApp[F[_]: ConcurrentEffect: Monad: Logger](
    client: Client[F]
  )(implicit T: Timer[F], C: ContextShift[F]): HttpApp[F] = {
    implicit val cfg: ConfigProvider.Config = ConfigProvider.provideConfig
    lazy val transactor = TransactorProvider.hikariTransactor(cfg)

    lazy val algorithmRepo: AlgorithmRepo[F] = AlgorithmRepo.impl
    lazy val realmRepo                       = RealmRepo.impl
    lazy val roleRepo                       = RoleRepo.impl
    lazy val userRepo                       = UserRepo.impl

    lazy val algorithmService = AlgorithmService.impl(algorithmRepo, transactor)
    lazy val realmService     = RealmService.impl(realmRepo, algorithmService, transactor)
    lazy val roleService = RoleService.impl(transactor, roleRepo)
    //todo: move user service abl into separate service
    lazy val userService = UserService.impl(transactor, userRepo, roleService,realmService, userRightsService)
    lazy val userRightsService = UserRightsService.impl(userService,roleService)
    // http
    val httpApp = DataManagerRoutes.algorithmRoutes[F](algorithmService)

    Router[F]("/algorithm" -> httpApp).orNotFound
  }
}
