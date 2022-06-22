package dev.rmaiun.arbiter2.helpers

import dev.rmaiun.arbiter2.dtos.stats
import dev.rmaiun.arbiter2.dtos.stats.{
  PlayerStats,
  SeasonShortStats,
  StatsCalcData,
  StatsState,
  Streak,
  StreakData,
  TmpStreak,
  UnrankedStats
}
import dev.rmaiun.arbiter2.utils.Constants
import dev.rmaiun.common.{ DateFormatter, SeasonHelper }
import dev.rmaiun.arbiter2.dtos.stats.StatsState.{ loser, winner }
import dev.rmaiun.arbiter2.dtos.stats._
import dev.rmaiun.protocol.http.GameDtoSet.StoredGameHistoryDto

import java.math.MathContext
import java.time.ZonedDateTime
import scala.annotation.tailrec
import scala.math.BigDecimal.RoundingMode

object StatsCalculator {

  def calculate(season: String, historyList: List[StoredGameHistoryDto]): SeasonShortStats = {
    val sortedHistory = historyList.sortBy(_.createdAt)
    val seasonGate    = SeasonHelper.seasonGate(season)
    val now           = DateFormatter.now.toLocalDate
    val daysToSeasonEnd = if (now.compareTo(seasonGate._2) > 0) {
      0
    } else {
      seasonGate._2.getDayOfYear - now.getDayOfYear
    }
    winLossShortStats(season, daysToSeasonEnd, sortedHistory)
  }

  private def winLossShortStats(
    season: String,
    daysToSeasonEnd: Int,
    rounds: List[StoredGameHistoryDto]
  ): SeasonShortStats = {
    val allRounds: List[StatsCalcData] = rounds.flatMap(r =>
      List(
        StatsCalcData(r.w1, 1, isWinner = true),
        StatsCalcData(r.w2, 1, isWinner = true),
        StatsCalcData(r.l1, 1, isWinner = false),
        StatsCalcData(r.l2, 1, isWinner = false)
      )
    )
    val groupedPlayerStats: Map[String, Int] = allRounds
      .groupBy(_.player)
      .map(e => (e._1, e._2.map(_.qty).reduceOption((a, b) => a + b).getOrElse(0)))
      .filter(e => e._2 >= 20)

    val winRounds: Map[String, Int] = allRounds
      .filter(_.isWinner)
      .groupMapReduce(_.player)(_.qty)(_ + _)

    val topPlayers: List[PlayerStats] = groupedPlayerStats
      .map(e => PlayerStats(e._1, preparePercentageStats(e, winRounds), e._2))
      .toList
      .sortBy(-_.score)

    val unrankedStats = allRounds
      .groupMapReduce(_.player)(_.qty)(_ + _)
      .filter(_._2 < 20)
      .map(e => UnrankedStats(e._1, Constants.expectedGames - e._2))
      .toList
      .sortBy(_.gamesToPlay)

    val (best, worst) = calculateStreaks(rounds)
    stats.SeasonShortStats(season, topPlayers, unrankedStats, rounds.size, daysToSeasonEnd, best, worst)
  }

  private def preparePercentageStats(e: (String, Int), winRounds: Map[String, Int]): BigDecimal = {
    val foundWins        = BigDecimal(winRounds.getOrElse(e._1, 0), new MathContext(4, java.math.RoundingMode.HALF_EVEN))
    val result           = foundWins / BigDecimal(e._2)
    val resultInPercents = result * BigDecimal(100)
    resultInPercents.setScale(2, RoundingMode.HALF_EVEN)
  }

  private def calculateStreaks(rounds: List[StoredGameHistoryDto]): (Option[Streak], Option[Streak]) = {
    val players = rounds
      .flatMap(r =>
        List(winner(r.w1, r.createdAt), winner(r.w2, r.createdAt), loser(r.l1, r.createdAt), loser(r.l2, r.createdAt))
      )

    val calculatedStreaks = calculateStreaksRecursively(players, Map())
    val bestStreaks = calculatedStreaks
      .map(e => TmpStreak(e._1, e._2.w, e._2.max))
      .toList
      .sortBy(ts => (-ts.shutout, ts.upd))

    val bestStreak = bestStreaks.headOption
      .map(ts => Streak(ts.player, ts.shutout))

    val worstStreaks = calculatedStreaks
      .map(e => TmpStreak(e._1, e._2.l, e._2.min))
      .toList
      .sortBy(ts => (-ts.shutout, ts.upd))

    val worstStreak = worstStreaks.headOption
      .map(ts => Streak(ts.player, ts.shutout))
    (bestStreak, worstStreak)
  }

  @tailrec
  private def calculateStreaksRecursively(
    players: List[StatsState],
    results: Map[String, StreakData]
  ): Map[String, StreakData] =
    players match {
      case ::(head, tail) =>
        val sd = streakData(results, head.player, head.score, head.created)
        calculateStreaksRecursively(tail, results ++ Map(head.player -> sd))
      case Nil => results
    }

  private def streakData(
    results: Map[String, StreakData],
    surname: String,
    score: Int,
    zdt: ZonedDateTime
  ): StreakData = {
    val found = results.getOrElse(surname, StreakData.default)
    if (score > 0) {
      val currentWin = found.curW + 1
      val maxWin     = if (currentWin > found.w) currentWin else found.w
      val maxDate    = if (currentWin > found.w) zdt else found.max
      StreakData(currentWin, maxWin, 0, found.l, maxDate, found.min)
    } else {
      val currentLose = found.curL + 1
      val maxLose     = if (currentLose > found.l) currentLose else found.l
      val minDate     = if (currentLose > found.l) zdt else found.min
      StreakData(0, found.w, currentLose, maxLose, found.max, minDate)
    }
  }
}
