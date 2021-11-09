package dev.rmaiun.mabel

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{ ConcurrentEffect, ContextShift, Timer }
import dev.profunktor.fs2rabbit.model.AmqpMessage
import dev.rmaiun.mabel.dtos.AmqpStructures
import dev.rmaiun.mabel.postprocessor.{ AddPlayerPostProcessor, AddRoundPostProcessor }
import dev.rmaiun.mabel.processors.{ AddPlayerProcessor, AddRoundProcessor, EloRatingProcessor, SeasonStatsProcessor }
import dev.rmaiun.mabel.routes.SysRoutes
import dev.rmaiun.mabel.services.ConfigProvider.ServerConfig
import dev.rmaiun.mabel.services._
import io.chrisdavenport.log4cats.Logger
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.server.Router

import scala.collection.immutable.Queue
case class Module[F[_]](httpApp: HttpApp[F], cmdHandler: CommandHandler[F], rlPublisher: RateLimitedPublisher[F])
object Module {
  type RateLimitQueue[F[_]] = Ref[F, Queue[AmqpMessage[String]]]

  def initHttpApp[F[_]: ConcurrentEffect: Monad: Logger](
    client: Client[F],
    amqpStructures: AmqpStructures[F],
    messagesRef: RateLimitQueue[F]
  )(implicit cfg: ServerConfig, T: Timer[F], C: ContextShift[F]): Module[F] = {

    lazy val arbiterClient        = ArbiterClient.impl(client)
    lazy val eloPointsCalculator  = EloPointsCalculator.impl(arbiterClient)
    lazy val publisherProxy       = PublisherProxy.impl(cfg, messagesRef)
    lazy val rateLimitedPublisher = RateLimitedPublisher.impl(messagesRef, amqpStructures.botOutputPublisher)

    // processors
    lazy val addPlayerProcessor   = AddPlayerProcessor.impl(arbiterClient)
    lazy val addRoundProcessor    = AddRoundProcessor.impl(arbiterClient, eloPointsCalculator)
    lazy val seasonStatsProcessor = SeasonStatsProcessor.impl(arbiterClient)
    lazy val eloRatingProcessor   = EloRatingProcessor.impl(arbiterClient)

    // post processors
    lazy val addPlayerPostProcessor = AddPlayerPostProcessor.impl(arbiterClient, publisherProxy)
    lazy val addRoundPostProcessor  = AddRoundPostProcessor.impl(arbiterClient, publisherProxy)

    // high lvl dependencies
    lazy val strategy =
      ProcessorStrategy.impl(
        addPlayerProcessor,
        addRoundProcessor,
        seasonStatsProcessor,
        eloRatingProcessor,
        addRoundPostProcessor,
        addPlayerPostProcessor
      )
    lazy val cmdHandler  = CommandHandler.impl(arbiterClient, strategy, publisherProxy)
    lazy val pingManager = PingManager.impl

    //http app
    val httpApp = Router[F](
      "/sys" -> SysRoutes.sysRoutes(pingManager)
    ).orNotFound

    // module
    Module(httpApp, cmdHandler, rateLimitedPublisher)
  }
}
