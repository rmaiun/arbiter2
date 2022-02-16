package dev.rmaiun.mabel.services

import cats.effect.Sync
import cats.syntax.apply._
import dev.profunktor.fs2rabbit.model.{ AmqpMessage, AmqpProperties }
import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.flowtypes.{ FLog, Flow }
import dev.rmaiun.mabel.Program.RateLimitQueue
import dev.rmaiun.mabel.dtos.BotResponse
import dev.rmaiun.mabel.services.ConfigProvider.Config
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

case class PublisherProxy[F[_]: Sync](cfg: Config, queue: RateLimitQueue[F]) {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)

  def publishToBot(botResponse: BotResponse)(implicit customCheck: Boolean = true): Flow[F, Unit] =
    if (cfg.app.notifications && botResponse.chatId != -1 && customCheck) {
      val output = BotResponse.BotResponseEncoder(botResponse).toString()
      val msg    = AmqpMessage(output, AmqpProperties())
      for {
        _ <- FLog.debug(s"Message was sent to rate limited queue $botResponse")
        _ <- Flow.effect(queue.modify(q => (q :+ msg, q)))
      } yield ()
    } else {
      FLog.debug(
        s"Message $botResponse wasn't sent to rate limited queue [notificationsEnabled:${cfg.app.notifications}, customCheck: $customCheck, chatId: ${botResponse.chatId}]"
      ) *> Flow.unit
    }
}
object PublisherProxy {
  def apply[F[_]](implicit ev: PublisherProxy[F]): PublisherProxy[F] = ev
  def impl[F[_]: Sync](cfg: Config, queue: RateLimitQueue[F]): PublisherProxy[F] =
    new PublisherProxy[F](cfg, queue)
}
