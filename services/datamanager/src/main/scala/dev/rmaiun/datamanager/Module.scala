package dev.rmaiun.datamanager

import cats.Monad
import cats.effect.{ ConcurrentEffect, ContextShift, Timer }
import dev.rmaiun.datamanager.helpers.{ ConfigProvider, TransactorProvider }
import dev.rmaiun.datamanager.managers.{ GameManager, RealmManager, UserManager }
import dev.rmaiun.datamanager.repositories._
import dev.rmaiun.datamanager.routes.DataManagerRoutes
import dev.rmaiun.datamanager.services._
import io.chrisdavenport.log4cats.Logger
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router

object Module {
  def initHttpApp[F[_]: ConcurrentEffect: Monad: Logger](
    client: Client[F]
  )(implicit T: Timer[F], C: ContextShift[F]): HttpApp[F] = {
    implicit val cfg: ConfigProvider.Config = ConfigProvider.provideConfig
    lazy val transactor                     = TransactorProvider.hikariTransactor(cfg)

    lazy val algorithmRepo: AlgorithmRepo[F] = AlgorithmRepo.impl
    lazy val realmRepo                       = RealmRepo.impl
    lazy val roleRepo                        = RoleRepo.impl
    lazy val userRepo                        = UserRepo.impl
    lazy val seasonRepo                      = SeasonRepo.impl
    lazy val gameRepo                        = GameRepo.impl

    lazy val algorithmService  = AlgorithmService.impl(algorithmRepo, transactor)
    lazy val realmService      = RealmService.impl(realmRepo, transactor)
    lazy val roleService       = RoleService.impl(transactor, roleRepo)
    lazy val seasonService     = SeasonService.impl(seasonRepo, realmService, algorithmService, transactor)
    lazy val userService       = UserService.impl(transactor, userRepo)
    lazy val userRightsService = UserRightsService.impl(userService, roleService)
    lazy val gameService       = GameService.impl(gameRepo, transactor)
    //managers
    lazy val realmMng = RealmManager.impl(realmService, algorithmService, userService)
    lazy val userMng  = UserManager.impl(userService, userRightsService, realmService, roleService, gameService)
    lazy val gameMng  = GameManager.impl(gameService, userService, realmService, seasonService, userRightsService)
    // http
    val realmHttpApp       = DataManagerRoutes.realmRoutes[F](realmMng)
    val userHttpApp        = DataManagerRoutes.userRoutes[F](userMng)
    val gameHistoryHttpApp = DataManagerRoutes.gameHistoryRoutes[F](gameMng)
    val eloPointsHttpApp   = DataManagerRoutes.eloPointsRoutes[F](gameMng)

    Router[F](
      "/realms"          -> realmHttpApp,
      "/users"           -> userHttpApp,
      "/games/history"   -> gameHistoryHttpApp,
      "/games/eloPoints" -> eloPointsHttpApp
    ).orNotFound
  }
}
