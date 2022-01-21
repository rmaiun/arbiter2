package dev.rmaiun.mabel.services

import cats.Show
import cats.effect.{ Concurrent, Sync }
import cats.syntax.apply._
import cats.syntax.show._
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{ FLog, Flow }
import dev.rmaiun.mabel.Program.InternalCache
import dev.rmaiun.mabel.services.ReportCache.{ defaultTTL, ReportKey }
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import scalacache.Mode

import scala.concurrent.duration._

case class ReportCache[F[_]: Sync: Concurrent](cache: InternalCache) {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)
  implicit val mode: Mode[F]                        = scalacache.CatsEffect.modes.async

  def find(key: ReportKey)(action: => Flow[F, Option[String]]): Flow[F, Option[String]] =
    get(key).flatMap {
      case p @ Some(_) =>
        FLog.info(s"Value found in cache for key: ${key.show}") *> Flow.pure(p)
      case None =>
        FLog.info(s"Value absent in cache for key: ${key.show}") *> action
    }

  def put(k: ReportKey, v: String): Flow[F, String] =
    Flow.effect(cache.doPut(k.show, v, defaultTTL)) *> Flow.pure(v)

  def put(k: ReportKey, v: Option[String]): Flow[F, Option[String]] =
    v match {
      case Some(value) =>
        Flow.effect(cache.doPut(k.show, value, defaultTTL)) *> Flow.pure(v)
      case None => Flow.pure(v)
    }

  def get(k: ReportKey): Flow[F, Option[String]] =
    Flow.effect(cache.doGet(k.show))

  def evict(k: ReportKey): Flow[F, Unit] =
    FLog.info(s"Evicting cache for key ${k.toString}") *>
      Flow.effect(cache.doRemove(k.show)).map(_ => ())
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
  def impl[F[_]: Sync: Concurrent](cache: InternalCache): ReportCache[F] =
    new ReportCache[F](cache)
}
