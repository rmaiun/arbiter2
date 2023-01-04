package dev.rmaiun.arbiter2.helpers

import cats.effect.Sync
import cats.effect.kernel.Clock
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{ FLog, Flow }
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

case class OperationalManager[F[_]: Clock: Sync](reportCache: ReportCache[F]) {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)

  def ping(): Flow[F, String] =
    for {
      c <- Flow.pure(Clock[F].realTime)
      _ <- FLog.info(s"current time $c")
    } yield c.toString

  def evictCache: Flow[F, Unit] = reportCache.evictAll
}

object OperationalManager {
  def apply[F[_]](implicit ev: OperationalManager[F]): OperationalManager[F] = ev
  def impl[F[_]: Clock: Sync](reportCache: ReportCache[F]): OperationalManager[F] =
    new OperationalManager[F](reportCache)
}
