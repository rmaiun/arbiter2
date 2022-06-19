package dev.rmaiun.mabel.helpers

import cats.effect.Sync
import cats.effect.kernel.Clock
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{FLog, Flow}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

case class PingManager[F[_]: Clock: Sync]() {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)

  def ping(): Flow[F, String] =
    for {
      c <- Flow.pure(Clock[F].realTime)
      _ <- FLog.info(s"current time $c")
    } yield c.toString
}

object PingManager {
  def apply[F[_]](implicit ev: PingManager[F]): PingManager[F] = ev
  def impl[F[_]: Clock: Sync]: PingManager[F] =
    new PingManager[F]()
}
