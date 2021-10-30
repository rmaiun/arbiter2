package dev.rmaiun.mabel.services

import cats.syntax.apply._
import dev.profunktor.fs2rabbit.model.{AmqpMessage, AmqpProperties}
import dev.rmaiun.flowtypes.Flow.{Flow, MonadThrowable}
import dev.rmaiun.flowtypes.{FLog, Flow}
import dev.rmaiun.mabel.dtos.AmqpStructures.AmqpPublisher
import dev.rmaiun.mabel.dtos.BotResponse
import dev.rmaiun.mabel.services.ConfigProvider.ServerConfig
import io.chrisdavenport.log4cats.Logger

case class CmdPublisher[F[_]: MonadThrowable: Logger](cfg: ServerConfig, botOutputPublisher: AmqpPublisher[F]) {
  def publishToBot(botResponse: BotResponse)(implicit customCheck: Boolean = true): Flow[F, Unit] =
    if (cfg.notificationsEnabled && customCheck) {
      val output = BotResponse.BotResponseEncoder(botResponse).toString()
      val msg    = AmqpMessage(output, AmqpProperties())
      FLog.info(s"Message was sent to bot $botResponse") *> Flow.effect(botOutputPublisher(msg))
    } else {
      FLog.info(
        s"Message $botResponse wasn't sent to bot [notificationsEnabled:${cfg.notificationsEnabled}, customCheck: $customCheck]"
      ) *> Flow.unit
    }
}
object CmdPublisher {
  def apply[F[_]](implicit ev: CmdPublisher[F]): CmdPublisher[F] = ev
  def impl[F[_]: MonadThrowable: Logger](cfg: ServerConfig, botOutputPublisher: AmqpPublisher[F]): CmdPublisher[F] =
    new CmdPublisher[F](cfg, botOutputPublisher)
}
