package dev.rmaiun.mabel.services

import cats.Monad
import cats.effect.{ Sync, Timer }
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{ FLog, Flow }
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.chrisdavenport.log4cats.{ Logger, SelfAwareStructuredLogger }

import java.util.concurrent.TimeUnit

case class PingManager[F[_]: Timer: Monad: Sync]() {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)

  def ping(): Flow[F, String] =
    for {
      c <- Flow.pure(Timer[F].clock.realTime(TimeUnit.MILLISECONDS))
      _ <- FLog.info(s"current time $c")
    } yield c.toString
}

object PingManager {
  def apply[F[_]](implicit ev: PingManager[F]): PingManager[F] = ev
  def impl[F[_]: Timer: Monad: Sync]: PingManager[F] =
    new PingManager[F]()
}
