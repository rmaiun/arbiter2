package dev.rmaiun.mabel.processors

import cats.Monad
import cats.syntax.apply._
import cats.syntax.foldable._
import dev.rmaiun.common.SeasonHelper
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{ FLog, Flow }
import dev.rmaiun.mabel.dtos._
import dev.rmaiun.mabel.dtos.stats.{ PlayerStats, SeasonShortStats, UnrankedStats }
import dev.rmaiun.mabel.services.ConfigProvider.AppCfg
import dev.rmaiun.mabel.services.{ ArbiterClient, PublisherProxy, StatsCalculator }
import dev.rmaiun.mabel.utils.Constants._
import dev.rmaiun.mabel.utils.IdGen
import dev.rmaiun.protocol.http.GameDtoSet.StoredGameHistoryDto
import dev.rmaiun.protocol.http.SeasonDtoSet.FindSeasonWithoutNotificationDtoOut
import dev.rmaiun.protocol.http.UserDtoSet.UserDto
import io.chrisdavenport.log4cats.Logger

import java.time.{ ZoneId, ZonedDateTime }

//todo move to postprocessor with processor forwarder
class SeasonResultsProcessor[F[_]: Monad: Logger](
  arbiterClient: ArbiterClient[F],
  publisherProxy: PublisherProxy[F],
  cfg: AppCfg
) extends Processor[F] {
  override def process(input: BotRequest): Flow[F, Option[ProcessorResponse]] =
    for {
      _ <- FLog.info(s"Starting Final Season Stats Reports generation with notificationEnabled: ${cfg.notifications}")
      _ <- processWithNotificationsCfgCheck()
    } yield None

  private def processWithNotificationsCfgCheck(): Flow[F, Unit] =
    if (cfg.notifications) {
      val now = ZonedDateTime.now(ZoneId.of(cfg.reportTimezone))
      for {
        _                <- FLog.info(s"Check that ${now.toString} equals to last day of current season where time = 20:00")
        seasonDto        <- arbiterClient.findSeasonWithoutNotifications
        notificationData <- takeNotificationData(seasonDto, now)
        _                <- processSeasonResultPreparation(notificationData)
      } yield ()
    } else {
      Flow.unit
    }

  private def processSeasonResultPreparation(notificationData: SeasonNotificationData): Flow[F, Unit] =
    notificationData match {
      case SeasonReady(season) =>
        for {
          messages <- prepareStatsData(season)
          _        <- FLog.info(s"Prepared ${messages.size} with season results")
          _        <- sendMessages(messages)
          _        <- FLog.info("Messages were successfully sent")
        } yield ()
      case _ =>
        Flow.unit
    }

  private def prepareStatsData(season: String): Flow[F, List[BotResponse]] =
    for {
      gameHistoryDto <- arbiterClient.listGameHistory(defaultRealm, season)
      players        <- loadActivePlayersForRealm
      stats          <- Flow.pure(StatsCalculator.calculate(season, gameHistoryDto.games))
      messages       <- prepareMessagesToSend(season, players, gameHistoryDto.games)
    } yield messages

  private def prepareMessagesToSend(
    season: String,
    players: Map[String, UserDto],
    games: List[StoredGameHistoryDto]
  ): Flow[F, List[BotResponse]] =
    Flow.pure {
      val stats = StatsCalculator.calculate(season, games)
      val ratingPlayers = stats.playersRating.zipWithIndex.flatMap { pr =>
        val player = players.get(pr._1.surname)
        val msg =
          s"""${messageHeader(stats)}
             |$DELIMITER
             |${ratingMessage(pr._1, pr._2)}
             |""".stripMargin.toBotMsg
        player.flatMap(_.tid).map(tid => BotResponse(tid, IdGen.msgId, msg))
      }
      val nonRatingPlayers = stats.unrankedStats.flatMap { us =>
        val player = players.get(us.player)
        val msg =
          s"""${messageHeader(stats)}
             |$DELIMITER
             |${nonRatingMessage(us)}
             |""".stripMargin.toBotMsg
        player.flatMap(_.tid).map(tid => BotResponse(tid, IdGen.msgId, msg))
      }
      ratingPlayers ++ nonRatingPlayers
    }

  private def ratingMessage(ps: PlayerStats, pos: Int): String =
    s"""Your achievements:
       |Position in rating - $pos
       |Win Rate - ${ps.score}%
       |Played games - ${ps.games}
       |👍👍👍""".stripMargin

  private def nonRatingMessage(us: UnrankedStats): String =
    s"""Your achievements:
       |Took part in ${20 - us.gamesToPlay} games.
       |Unfortunately, need to play 20 games
       |to be included into rating.
       |Hope, that next time you will do your best
       |and will show us your real power.
       |⭐⭐⭐""".stripMargin

  private def messageHeader(stats: SeasonShortStats): String =
    s"""Congrats!
       |Season ${stats.season} is successfully finished.
       |Our winner - ${stats.playersRating.headOption.fold("n/a")(pr => pr.surname)}
       |${stats.playersRating.length} players played ${stats.gamesPlayed} in total.""".stripMargin

  private def loadActivePlayersForRealm: Flow[F, Map[String, UserDto]] =
    arbiterClient.findAllPlayers
      .map(dtoOut => dtoOut.items.map(i => i.surname -> i).toMap)
  private def takeNotificationData(
    seasonDto: FindSeasonWithoutNotificationDtoOut,
    now: ZonedDateTime
  ): Flow[F, SeasonNotificationData] =
    seasonDto.season match {
      case Some(value) =>
        val isReady   = SeasonHelper.firstBeforeSecond(value.name, SeasonHelper.currentSeason)
        val isNotLate = notLateToSend(now)
        if (isReady && isNotLate) {
          FLog.info("Current timestamp passed criteria for sending season results") *>
            Flow.pure(SeasonReady(value.name))
        } else {
          FLog.info("Current timestamp didn't pass criteria for sending season results") *>
            Flow.pure(SeasonNotReady(value.name))
        }
      case None =>
        FLog.info("There are no seasons with unprocessed results") *>
          Flow.pure(SeasonAbsent())
    }

  private def sendMessages(messages: List[BotResponse]): Flow[F, Unit] =
    messages.map(m => publisherProxy.publishToBot(m)).sequence_

  private def notLateToSend(dateTime: ZonedDateTime): Boolean =
    dateTime.getHour >= 10 && dateTime.getHour <= 23

}
object SeasonResultsProcessor {
  def apply[F[_]](implicit ev: SeasonResultsProcessor[F]): SeasonResultsProcessor[F] = ev
  def impl[F[_]: Monad: Logger](
    ac: ArbiterClient[F],
    publisherProxy: PublisherProxy[F],
    cfg: AppCfg
  ): SeasonResultsProcessor[F] =
    new SeasonResultsProcessor[F](ac, publisherProxy, cfg)
}
