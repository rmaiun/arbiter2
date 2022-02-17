package dev.rmaiun.mabel

import cats.effect.{ Async, Clock, Ref }
import com.github.blemale.scaffeine.{ Cache, Scaffeine }
import dev.profunktor.fs2rabbit.model.AmqpMessage
import dev.rmaiun.mabel.dtos.{ AmqpStructures, CmdType }
import dev.rmaiun.mabel.postprocessor.{
  AddPlayerPostProcessor,
  AddRoundPostProcessor,
  BroadcastMessagePostProcessor,
  SeasonResultPostProcessor
}
import dev.rmaiun.mabel.processors._
import dev.rmaiun.mabel.routes.SysRoutes
import dev.rmaiun.mabel.services.ConfigProvider.Config
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
  queryPublisher: SeasonResultsTrigger[F]
)
object Program {
  type RateLimitQueue[F[_]] = Ref[F, Queue[AmqpMessage[String]]]
  type InternalCache        = Cache[String, String]

  def initHttpApp[F[_]: Async: Logger](
    client: Client[F],
    amqpStructures: AmqpStructures[F],
    messagesRef: RateLimitQueue[F]
  )(implicit cfg: Config, T: Clock[F]): Program[F] = {

    lazy val cache: Cache[String, String] = Scaffeine()
      .recordStats()
      .expireAfterWrite(4.hour)
      .maximumSize(500)
      .build[String, String]()

    lazy val reportCache          = ReportCache.impl(cache)
    lazy val arbiterClient        = ArbiterClient.impl(client)
    lazy val eloPointsCalculator  = EloPointsCalculator.impl(arbiterClient)
    lazy val publisherProxy       = PublisherProxy.impl(cfg, messagesRef)
    lazy val rateLimitedPublisher = RateLimitedPublisher.impl(messagesRef, amqpStructures.botOutPublisher)

    // processors
    lazy val processors = List(
      AddPlayerProcessor.impl(arbiterClient),
      AddRoundProcessor.impl(arbiterClient, eloPointsCalculator, reportCache),
      ShortSeasonStatsProcessor.impl(arbiterClient, reportCache),
      EloRatingProcessor.impl(arbiterClient, reportCache),
      LastGamesProcessor.impl(arbiterClient),
      BroadcastMessageProcessor.impl(arbiterClient),
      ForwardProcessor.impl
    )
    // post processors
    lazy val postProcessors = List(
      AddPlayerPostProcessor.impl(arbiterClient, publisherProxy),
      AddRoundPostProcessor.impl(arbiterClient, publisherProxy),
      SeasonResultPostProcessor.impl(arbiterClient, publisherProxy, cfg.app),
      BroadcastMessagePostProcessor.impl(arbiterClient, publisherProxy)
    )
    lazy val persistenceCmdHandler =
      CommandHandler.impl(CmdType.Persistence)(arbiterClient, processors, postProcessors, publisherProxy)
    lazy val queryCmdHandler =
      CommandHandler.impl(CmdType.Query)(arbiterClient, processors, postProcessors, publisherProxy)
    lazy val pingManager = PingManager.impl

    //http app
    lazy val httpApp = Router[F](
      "/sys" -> SysRoutes.sysRoutes(pingManager)
    ).orNotFound
    // query publisher
    lazy val seasonResultsTrigger = SeasonResultsTrigger.impl(arbiterClient, amqpStructures.botInPublisher)
    // module
    Program(httpApp, persistenceCmdHandler, queryCmdHandler, rateLimitedPublisher, seasonResultsTrigger)
  }
}
