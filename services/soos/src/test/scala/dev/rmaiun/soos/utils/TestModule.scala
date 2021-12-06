package dev.rmaiun.soos.utils

import cats.effect._
import dev.rmaiun.soos.helpers.{ ConfigProvider, TransactorProvider }
import dev.rmaiun.soos.managers.{ GameManager, RealmManager, SeasonManager, UserManager }
import dev.rmaiun.soos.repositories._
import dev.rmaiun.soos.services._
import doobie.hikari.HikariTransactor

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
object TestModule extends Loggable {
  implicit val cs: ContextShift[IO] = IO.contextShift(
    ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  )
  implicit val cfg: ConfigProvider.Config   = ConfigProvider.provideConfig
  lazy val transactor: HikariTransactor[IO] = TransactorProvider.hikariTransactor[IO](cfg)

  lazy val algorithmRepo: AlgorithmRepo[IO] = AlgorithmRepo.impl[IO]
  lazy val realmRepo: RealmRepo[IO]         = RealmRepo.impl[IO]
  lazy val roleRepo: RoleRepo[IO]           = RoleRepo.impl[IO]
  lazy val userRepo: UserRepo[IO]           = UserRepo.impl[IO]
  lazy val seasonRepo: SeasonRepo[IO]       = SeasonRepo.impl[IO]
  lazy val gameRepo: GameRepo[IO]           = GameRepo.impl[IO]

  lazy val algorithmService: AlgorithmService[IO] = AlgorithmService.impl[IO](algorithmRepo, transactor)
  lazy val realmService: RealmService[IO]         = RealmService.impl[IO](realmRepo, transactor)
  lazy val roleService: RoleService[IO]           = RoleService.impl[IO](transactor, roleRepo)
  lazy val seasonService: SeasonService[IO] =
    SeasonService.impl[IO](seasonRepo, realmService, algorithmService, transactor)
  lazy val userService: UserService[IO]             = UserService.impl[IO](transactor, userRepo)
  lazy val userRightsService: UserRightsService[IO] = UserRightsService.impl[IO](userService, roleService)
  lazy val gameService: GameService[IO]             = GameService.impl[IO](gameRepo, transactor)
  //managers
  lazy val realmMng: RealmManager[IO] = RealmManager.impl(realmService, algorithmService, userService)
  lazy val userMng: UserManager[IO] =
    UserManager.impl(userService, userRightsService, realmService, roleService, gameService)
  lazy val gameMng: GameManager[IO] =
    GameManager.impl(gameService, userService, realmService, seasonService, userRightsService)
  lazy val seasonMng: SeasonManager[IO] = SeasonManager.impl(algorithmService, realmService, seasonService, cfg.app)
}
