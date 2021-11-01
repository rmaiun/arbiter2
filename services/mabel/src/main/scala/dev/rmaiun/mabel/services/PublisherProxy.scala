package dev.rmaiun.mabel.services

import cats.syntax.apply._
import dev.profunktor.fs2rabbit.model.{ AmqpMessage, AmqpProperties }
import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.flowtypes.{ FLog, Flow }
import dev.rmaiun.mabel.Module.RateLimitQueue
import dev.rmaiun.mabel.dtos.BotResponse
import dev.rmaiun.mabel.services.ConfigProvider.ServerConfig
import io.chrisdavenport.log4cats.Logger

case class PublisherProxy[F[_]: MonadThrowable: Logger](cfg: ServerConfig, queue: RateLimitQueue[F]) {
  def publishToBot(botResponse: BotResponse)(implicit customCheck: Boolean = true): Flow[F, Unit] =
    if (cfg.notificationsEnabled && customCheck) {
      val output = BotResponse.BotResponseEncoder(botResponse).toString()
      val msg    = AmqpMessage(output, AmqpProperties())
      for {
        _ <- FLog.debug(s"Message was sent to rate limited queue $botResponse")
        _ <- Flow.effect(queue.modify(q => (q :+ msg, q)))
      } yield ()
    } else {
      FLog.debug(
        s"Message $botResponse wasn't sent to rate limited queue [notificationsEnabled:${cfg.notificationsEnabled}, customCheck: $customCheck]"
      ) *> Flow.unit
    }
}
object PublisherProxy {
  def apply[F[_]](implicit ev: PublisherProxy[F]): PublisherProxy[F] = ev
  def impl[F[_]: MonadThrowable: Logger](cfg: ServerConfig, queue: RateLimitQueue[F]): PublisherProxy[F] =
    new PublisherProxy[F](cfg, queue)
}
