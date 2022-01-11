package dev.rmaiun.mabel.services

import cats.Show
import cats.effect.Sync
import cats.syntax.apply._
import cats.syntax.show._
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{ FLog, Flow }
import dev.rmaiun.mabel.Program.InternalCache
import dev.rmaiun.mabel.dtos.ProcessorResponse
import dev.rmaiun.mabel.services.ReportCache.ReportKey
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

case class ReportCache[F[_]: Sync](cache: InternalCache) {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)

  def find(key: ReportKey)(action: => Flow[F, Option[ProcessorResponse]]): Flow[F, Option[ProcessorResponse]] =
    get(key).flatMap {
      case p @ Some(_) =>
        FLog.info(s"Value found in cache for key: ${key.show}") *> Flow.pure(p)
      case None =>
        FLog.info(s"Value absent in cache for key: ${key.show}") *> action
    }

  def put(k: ReportKey, v: ProcessorResponse): Flow[F, ProcessorResponse] =
    Flow.effect(Sync[F].delay(cache.put(k.show, v))) *> Flow.pure(v)

  def put(k: ReportKey, v: Option[ProcessorResponse]): Flow[F, Option[ProcessorResponse]] =
    v match {
      case Some(value) =>
        Flow.effect(Sync[F].delay(cache.put(k.show, value))) *> Flow.pure(v)
      case None => Flow.pure(v)
    }

  def get(k: ReportKey): Flow[F, Option[ProcessorResponse]] =
    Flow.effect(Sync[F].delay(cache.getIfPresent(k.show)))

  def evict(k: ReportKey): Flow[F, Unit] =
    FLog.info(s"Evicting cache for key ${k.toString}") *>
      Flow.effect(Sync[F].delay(cache.invalidate(k.show)))
}
object ReportCache {
  sealed trait ReportKey
  case object EloRatingReport extends ReportKey
  case object SeasonReport    extends ReportKey

  implicit val ShowReportKey: Show[ReportKey] = Show.show {
    case EloRatingReport => "eloRatingReport"
    case SeasonReport    => "seasonReport"
  }

  def apply[F[_]](implicit ev: ReportCache[F]): ReportCache[F] = ev
  def impl[F[_]: Sync](cache: InternalCache): ReportCache[F] =
    new ReportCache[F](cache)
}
