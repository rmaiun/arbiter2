package dev.rmaiun.mabel

import cats.Monad
import cats.effect.{ ConcurrentEffect, ContextShift, Timer }
import dev.rmaiun.mabel.dtos.AmqpStructures
import dev.rmaiun.mabel.processors.{ AddPlayerProcessor, AddRoundProcessor, EloRatingProcessor, SeasonStatsProcessor }
import dev.rmaiun.mabel.routes.SysRoutes
import dev.rmaiun.mabel.services._
import io.chrisdavenport.log4cats.Logger
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.server.Router

object Module {
  def initHttpApp[F[_]: ConcurrentEffect: Monad: Logger](
    client: Client[F],
    amqpStructures: AmqpStructures[F]
  )(implicit T: Timer[F], C: ContextShift[F]): (HttpApp[F], CommandHandler[F]) = {
    lazy val arbiterClient        = ArbiterClient.impl(client)
    lazy val eloPointsCalculator  = EloPointsCalculator.impl(arbiterClient)
    lazy val addPlayerProcessor   = AddPlayerProcessor.impl(arbiterClient)
    lazy val addRoundProcessor    = AddRoundProcessor.impl(arbiterClient, eloPointsCalculator)
    lazy val seasonStatsProcessor = SeasonStatsProcessor.impl(arbiterClient)
    lazy val eloRatingProcessor   = EloRatingProcessor.impl(arbiterClient)
    lazy val strategy =
      ProcessorStrategy.impl(addPlayerProcessor, addRoundProcessor, seasonStatsProcessor, eloRatingProcessor)
    lazy val cmdHandler  = CommandHandler.impl(strategy, amqpStructures.botOutputPublisher)
    lazy val pingManager = PingManager.impl

    (
      Router[F](
        "/sys" -> SysRoutes.sysRoutes(pingManager)
      ).orNotFound,
      cmdHandler
    )
  }
}
