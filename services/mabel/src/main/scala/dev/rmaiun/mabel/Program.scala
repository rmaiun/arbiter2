package dev.rmaiun.mabel

import cats.effect.{Async, Clock, Ref}
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import dev.profunktor.fs2rabbit.model.AmqpMessage
import dev.rmaiun.mabel.dtos.{AmqpStructures, CmdType}
import dev.rmaiun.mabel.helpers.{CommandHandler, DumpExporter, EloPointsCalculator, PingManager, PublisherProxy, RateLimitedPublisher, ReportCache, SeasonResultsTrigger, TransactorProvider, ZipDataProvider}
import dev.rmaiun.mabel.helpers.ConfigProvider.Config
import dev.rmaiun.mabel.managers.{GameManager, RealmManager, SeasonManager, UserManager}
import dev.rmaiun.mabel.postprocessor.{AddPlayerPostProcessor, AddRoundPostProcessor, BroadcastMessagePostProcessor, SeasonResultPostProcessor}
import dev.rmaiun.mabel.processors._
import dev.rmaiun.mabel.repositories._
import dev.rmaiun.mabel.routes.SysRoutes
import dev.rmaiun.mabel.services._
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

import scala.collection.immutable.Queue
import scala.concurrent.duration._

case class Program[F[_]](
  httpApp: HttpApp[F],
  persistHandler: CommandHandler[F],
  queryHandler: CommandHandler[F],
  rlPublisher: RateLimitedPublisher[F],
  queryPublisher: SeasonResultsTrigger[F],
  dumpExporter: DumpExporter[F]
)
object Program {
  type RateLimitQueue[F[_]] = Ref[F, Queue[AmqpMessage[String]]]
  type InternalCache        = Cache[String, String]

  def initHttpApp[F[_]: Async: Logger](
    client: Client[F],
    amqpStructures: AmqpStructures[F],
    messagesRef: RateLimitQueue[F]
  )(implicit cfg: Config, T: Clock[F]): Program[F] = {
    lazy val transactor = TransactorProvider.hikariTransactor(cfg)

    lazy val algorithmRepo = AlgorithmRepo.impl
    lazy val realmRepo     = RealmRepo.impl
    lazy val roleRepo      = RoleRepo.impl
    lazy val userRepo      = UserRepo.impl
    lazy val seasonRepo    = SeasonRepo.impl
    lazy val gameRepo      = GameRepo.impl

    lazy val algorithmService  = AlgorithmService.impl(algorithmRepo, transactor)
    lazy val realmService      = RealmService.impl(realmRepo, transactor)
    lazy val roleService       = RoleService.impl(transactor, roleRepo)
    lazy val seasonService     = SeasonService.impl(seasonRepo, realmService, algorithmService, transactor)
    lazy val userService       = UserService.impl(transactor, userRepo)
    lazy val userRightsService = UserRightsService.impl(userService, roleService)
    lazy val gameService       = GameService.impl(gameRepo, transactor)

    lazy val zipDataProvider =
      ZipDataProvider.impl(algorithmRepo, roleRepo, realmRepo, gameRepo, seasonRepo, userRepo, transactor)
    lazy val dumpExporter = DumpExporter.impl(zipDataProvider, cfg)
    // managers
    lazy val realmMng  = RealmManager.impl(realmService, algorithmService, userService)
    lazy val userMng   = UserManager.impl(userService, userRightsService, realmService, roleService, gameService)
    lazy val gameMng   = GameManager.impl(gameService, userService, realmService, seasonService, userRightsService)
    lazy val seasonMng = SeasonManager.impl(algorithmService, realmService, seasonService, cfg.app)

    lazy val cache: Cache[String, String] = Scaffeine()
      .recordStats()
      .expireAfterWrite(4.hour)
      .maximumSize(500)
      .build[String, String]()

    lazy val reportCache          = ReportCache.impl(cache)
    lazy val eloPointsCalculator  = EloPointsCalculator.impl(gameMng)
    lazy val publisherProxy       = PublisherProxy.impl(cfg, messagesRef)
    lazy val rateLimitedPublisher = RateLimitedPublisher.impl(messagesRef, amqpStructures.botOutPublisher)

    // processors
    lazy val processors = List(
      AddPlayerProcessor.impl(userMng),
      AddRoundProcessor.impl(gameMng, userMng, eloPointsCalculator, reportCache),
      ShortSeasonStatsProcessor.impl(gameMng, reportCache),
      EloRatingProcessor.impl(gameMng, userMng, reportCache),
      LastGamesProcessor.impl(gameMng),
      BroadcastMessageProcessor.impl(userMng),
      ForwardProcessor.impl
    )
    // post processors
    lazy val postProcessors = List(
      AddPlayerPostProcessor.impl(userMng, publisherProxy),
      AddRoundPostProcessor.impl(userMng, publisherProxy),
      SeasonResultPostProcessor.impl(seasonMng, gameMng, userMng, publisherProxy, cfg.app),
      BroadcastMessagePostProcessor.impl(userMng, publisherProxy)
    )
    lazy val persistenceCmdHandler =
      CommandHandler.impl(CmdType.Persistence)(userMng, processors, postProcessors, publisherProxy)
    lazy val queryCmdHandler =
      CommandHandler.impl(CmdType.Query)(userMng, processors, postProcessors, publisherProxy)
    lazy val pingManager = PingManager.impl

    //http app
    lazy val httpApp = Router[F](
      "/sys" -> SysRoutes.sysRoutes(pingManager)
    ).orNotFound
    // query publisher
    lazy val seasonResultsTrigger = SeasonResultsTrigger.impl(userMng, amqpStructures.botInPublisher)
    // module
    Program(httpApp, persistenceCmdHandler, queryCmdHandler, rateLimitedPublisher, seasonResultsTrigger, dumpExporter)
  }
}
