package dev.rmaiun.mabel.processors

import cats.Monad
import dev.rmaiun.common.{DateFormatter, SeasonHelper}
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{FLog, Flow}
import dev.rmaiun.mabel.dtos.{BotRequest, ProcessorResponse}
import dev.rmaiun.mabel.services.ArbiterClient
import dev.rmaiun.mabel.services.ConfigProvider.AppCfg
import io.chrisdavenport.log4cats.Logger

import java.time.{ZoneId, ZonedDateTime}

class SeasonResultsProcessor[F[_]: Monad: Logger](arbiterClient: ArbiterClient[F], cfg: AppCfg) extends Processor[F] {
  override def process(input: BotRequest): Flow[F, Option[ProcessorResponse]] =
    for {
      _ <- FLog.info(s"Starting Final Season Stats Reports generation with notificationEnabled: ${cfg.notifications}")
      _ <- processWithNotificationsCfgCheck()
    } yield ()

  private def processWithNotificationsCfgCheck(): Flow[F, Unit] =
    if (cfg.notifications) {
      val date = ZonedDateTime.now(ZoneId.of(cfg.reportTimezone))
      for{
        _ <- FLog.info(s"Check that ${date.toString} equals to last day of current season where time = 20:00")
      }yield
      SeasonHelper
    } else {
      Flow.unit
    }

  private def notLateToSend(dateTime:ZonedDateTime ):Boolean = {
    dateTime.getHour >= 10 && dateTime.getHour <= 23;
  }
  private def utcToEet(utcDateTime: ZonedDateTime ):ZonedDateTime= {
    utcDateTime.withZoneSameInstant(ZoneId.of(cfg.reportTimezone));
  }
}
object SeasonResultsProcessor {
  def apply[F[_]](implicit ev: SeasonResultsProcessor[F]): SeasonResultsProcessor[F] = ev
  def impl[F[_]: Monad: Logger](ac: ArbiterClient[F], cfg: AppCfg): SeasonResultsProcessor[F] =
    new SeasonResultsProcessor[F](ac, cfg)
}
