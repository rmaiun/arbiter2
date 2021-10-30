package dev.rmaiun.mabel.processors

import cats.Monad
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.commands.SeasonStatsCmd
import dev.rmaiun.mabel.dtos.stats.SeasonShortStats
import dev.rmaiun.mabel.dtos.{ BotRequest, ProcessorResponse }
import dev.rmaiun.mabel.services.{ ArbiterClient, StatsCalculator }
import dev.rmaiun.mabel.utils.Constants._
import dev.rmaiun.mabel.utils.IdGen
import io.chrisdavenport.log4cats.Logger

class SeasonStatsProcessor[F[_]: Monad](ac: ArbiterClient[F]) extends Processor[F] {
  override def process(input: BotRequest): Flow[F, ProcessorResponse] =
    for {
      dto         <- parseDto[SeasonStatsCmd](input.data)
      historyList <- ac.listGameHistory(defaultRealm, dto.season)
    } yield {
      val stats = StatsCalculator.calculate(dto.season, historyList.games)
      val msg   = message(stats)
      ProcessorResponse.ok(input.chatId, IdGen.msgId, msg)
    }

  private def message(data: SeasonShortStats): String =
    if (data.gamesPlayed == 0) {
      s"$PREFIX No games found for season ${data.season} $SUFFIX"
    } else {
      val ratings = if (data.playersRating.isEmpty) {
        "Nobody played more than 20 games"
      } else {
        data.playersRating.zipWithIndex
          .map(e => s"${e._2 + 1}. ${e._1.surname.capitalize} ${e._1.score} %")
          .mkString(LINE_SEPARATOR)
      }
      val bestStreak = data.bestStreak
        .map(s => s"${s.player.capitalize}: ${s.games} in row")
        .getOrElse(DEFAULT_RESULT)
      val worstStreak = data.worstStreak
        .map(s => s"${s.player.capitalize}: ${s.games} in row")
        .getOrElse(DEFAULT_RESULT)
      val separator = "-" * 30
      PREFIX ++ s"""Season: ${data.season}
                   |Games played: ${data.gamesPlayed}
                   |Season ends in: ${data.daysToSeasonEnd} days
                   |$separator
                   |Rating:
                   |$ratings
                   |${formatUnrankedPlayers(separator, data)}
                   |$separator
                   |Best streak:
                   |$bestStreak
                   |$separator
                   |Worst streak
                   |$worstStreak
                   |$SUFFIX
                   |""".stripMargin
    }

  private def formatUnrankedPlayers(separator: String, data: SeasonShortStats): String =
    if (data.unrankedStats.isEmpty) {
      DEFAULT_RESULT
    } else {
      val unrankedData = data.unrankedStats
        .map(us => s"- ${us.player.capitalize} +${us.gamesToPlay}")
        .mkString(LINE_SEPARATOR)
      s"""$separator
         |Waiting it rating:
         |$unrankedData""".stripMargin
    }

}

object SeasonStatsProcessor {
  def apply[F[_]](implicit ev: SeasonStatsProcessor[F]): SeasonStatsProcessor[F] = ev
  def impl[F[_]: Monad: Logger](ac: ArbiterClient[F]): SeasonStatsProcessor[F] =
    new SeasonStatsProcessor[F](ac)
}
