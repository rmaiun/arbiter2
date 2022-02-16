package dev.rmaiun.mabel.services

import cats.Show
import cats.effect.Sync
import cats.syntax.show._
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{ FLog, Flow }
import dev.rmaiun.mabel.Program.InternalCache
import dev.rmaiun.mabel.services.ReportCache.ReportKey
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration._

case class ReportCache[F[_]: Sync](cache: InternalCache) {
  implicit val logger: Logger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)

  def find(key: ReportKey)(action: => Flow[F, Option[String]]): Flow[F, Option[String]] =
    get(key).flatMap {
      case p @ Some(_) =>
        FLog.info(s"Value found in cache for key: ${key.show}").flatMap(_ => Flow.pure(p))
      case None =>
        FLog.info(s"Value absent in cache for key: ${key.show}").flatMap(_ => action)
    }

  def put(k: ReportKey, v: String): Flow[F, String] =
    Flow.effect(Sync[F].delay(cache.put(k.show, v))).flatMap(_ => Flow.pure(v))

  def put(k: ReportKey, v: Option[String]): Flow[F, Option[String]] =
    v match {
      case Some(value) =>
        Flow
          .effect(Sync[F].delay(cache.put(k.show, value)))
          .flatMap(_ => Flow.pure(v))
      case None => Flow.pure(v)
    }

  def get(k: ReportKey): Flow[F, Option[String]] =
    Flow.effect(Sync[F].delay(cache.getIfPresent(k.show)))

  def evict(k: ReportKey): Flow[F, Unit] =
    FLog
      .info(s"Evicting cache for key ${k.toString}")
      .flatMap(_ => Flow.effect(Sync[F].delay(cache.invalidate(k.show))).map(_ => ()))

}
object ReportCache {
  sealed trait ReportKey
  case object EloRatingReport extends ReportKey
  case object SeasonReport    extends ReportKey

  implicit val ShowReportKey: Show[ReportKey] = Show.show {
    case EloRatingReport => "eloRatingReport"
    case SeasonReport    => "seasonReport"
  }
  val defaultTTL: Option[Duration] = Some(4 hours)

  def apply[F[_]](implicit ev: ReportCache[F]): ReportCache[F] = ev
  def impl[F[_]: Sync](cache: InternalCache): ReportCache[F] =
    new ReportCache[F](cache)
}
