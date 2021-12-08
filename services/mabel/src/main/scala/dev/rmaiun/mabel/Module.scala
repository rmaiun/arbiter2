package dev.rmaiun.mabel

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{ ConcurrentEffect, ContextShift, Timer }
import dev.profunktor.fs2rabbit.model.AmqpMessage
import dev.rmaiun.mabel.dtos.{ AmqpStructures, CmdType }
import dev.rmaiun.mabel.postprocessor.{ AddPlayerPostProcessor, AddRoundPostProcessor, SeasonResultPostProcessor }
import dev.rmaiun.mabel.processors._
import dev.rmaiun.mabel.routes.SysRoutes
import dev.rmaiun.mabel.services.ConfigProvider.Config
import dev.rmaiun.mabel.services._
import io.chrisdavenport.log4cats.Logger
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.server.Router

import scala.collection.immutable.Queue
case class Module[F[_]](
  httpApp: HttpApp[F],
  persistHandler: CommandHandler[F],
  queryHandler: CommandHandler[F],
  rlPublisher: RateLimitedPublisher[F],
  queryPublisher: SeasonResultsTrigger[F]
)
object Module {
  type RateLimitQueue[F[_]] = Ref[F, Queue[AmqpMessage[String]]]

  def initHttpApp[F[_]: ConcurrentEffect: Monad: Logger](
    client: Client[F],
    amqpStructures: AmqpStructures[F],
    messagesRef: RateLimitQueue[F]
  )(implicit cfg: Config, T: Timer[F], C: ContextShift[F]): Module[F] = {

    lazy val arbiterClient        = ArbiterClient.impl(client)
    lazy val eloPointsCalculator  = EloPointsCalculator.impl(arbiterClient)
    lazy val publisherProxy       = PublisherProxy.impl(cfg, messagesRef)
    lazy val rateLimitedPublisher = RateLimitedPublisher.impl(messagesRef, amqpStructures.botOutPublisher)

    // processors
    lazy val processors = List(
      AddPlayerProcessor.impl(arbiterClient),
      AddRoundProcessor.impl(arbiterClient, eloPointsCalculator),
      ShortSeasonStatsProcessor.impl(arbiterClient),
      EloRatingProcessor.impl(arbiterClient),
      LastGamesProcessor.impl(arbiterClient),
      ForwardProcessor.impl
    )
    // post processors
    lazy val postProcessors = List(
      AddPlayerPostProcessor.impl(arbiterClient, publisherProxy),
      AddRoundPostProcessor.impl(arbiterClient, publisherProxy),
      SeasonResultPostProcessor.impl(arbiterClient, publisherProxy, cfg.app)
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
    Module(httpApp, persistenceCmdHandler, queryCmdHandler, rateLimitedPublisher, seasonResultsTrigger)
  }
}
