package dev.rmaiun.arbiter2.helpers

import cats.Monad
import cats.syntax.option._
import dev.rmaiun.arbiter2.dtos.EloRatingDto.{ EloPlayers, UserCalculatedPoints }
import dev.rmaiun.arbiter2.managers.GameManager
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.arbiter2.dtos.EloRatingDto._
import dev.rmaiun.arbiter2.errors.Errors.UnavailableUsersFound
import dev.rmaiun.protocol.http.GameDtoSet.{ ListEloPointsDtoIn, ListEloPointsDtoOut }
case class EloPointsCalculator[F[_]: Monad](
  gameManager: GameManager[F]
) {
  def calculate(dto: EloPlayers): Flow[F, UserCalculatedPoints] =
    for {
      loadedPoints <- loadEloPoints(dto)
    } yield {
      val avgWinPoints = loadedPoints.calculatedEloPoints
        .filter(x => List(dto.w1, dto.w2).contains(x.user))
        .map(_.value)
        .sum / 2
      val avgLosePoints = loadedPoints.calculatedEloPoints
        .filter(x => List(dto.l1, dto.l2).contains(x.user))
        .map(_.value)
        .sum / 2
      val w1 = loadedPoints.calculatedEloPoints.filter(x => x.user == dto.w1).head
      val w2 = loadedPoints.calculatedEloPoints.filter(x => x.user == dto.w2).head
      val l1 = loadedPoints.calculatedEloPoints.filter(x => x.user == dto.l1).head
      val l2 = loadedPoints.calculatedEloPoints.filter(x => x.user == dto.l2).head

      val kW1 = findK(w1.value, w1.gamesPlayed)
      val kW2 = findK(w2.value, w2.gamesPlayed)
      val kL1 = findK(l1.value, l1.gamesPlayed)
      val kL2 = findK(l2.value, l2.gamesPlayed)

      val ratingW1 = eloAlgorithmRunWin(w1.value, avgLosePoints, kW1)
      val ratingW2 = eloAlgorithmRunWin(w2.value, avgLosePoints, kW2)
      val ratingL1 = eloAlgorithmRunLose(avgWinPoints, l1.value, kL1)
      val ratingL2 = eloAlgorithmRunLose(avgWinPoints, l2.value, kL2)
      UserCalculatedPoints(
        CalculatedPoints(w1.user, ratingW1 - w1.value),
        CalculatedPoints(w2.user, ratingW2 - w2.value),
        CalculatedPoints(l1.user, ratingL1 - l1.value),
        CalculatedPoints(l2.user, ratingL2 - l2.value)
      )
    }

  private def loadEloPoints(dto: EloPlayers): Flow[F, ListEloPointsDtoOut] = {
    val users = List(dto.w1, dto.w2, dto.l1, dto.l2)
    gameManager.listCalculatedEloPoints(ListEloPointsDtoIn(users.some)).flatMap { data =>
      if (data.unratedPlayers.isEmpty) {
        Flow.pure(data)
      } else {
        Flow.error(UnavailableUsersFound(data.unratedPlayers))
      }
    }
  }

  private def eloAlgorithmRunWin(rA: Int, rB: Int, k: Int): Int = {
    val eA           = expectedRating(rB.toFloat - rA.toFloat)
    val winnerPoints = rA + k * (1 - eA)
    Math.round(winnerPoints)
  }

  private def eloAlgorithmRunLose(rA: Int, rB: Int, k: Int): Int = {
    val eB          = expectedRating(rA.toFloat - rB.toFloat)
    val loserPoints = rB + k * (0 - eB)
    Math.round(loserPoints)
  }

  private def expectedRating(ratingDiff: Float): Float = {
    val ratingDiffDivided = ratingDiff / 400
    val divisor           = Math.pow(10, 1.0f * ratingDiffDivided).toFloat
    1.0f / (1 + divisor)
  }

  private def findK(rating: Int, games: Int): Int =
    if (games <= 30) {
      40
    } else if (rating >= 2400) {
      10
    } else {
      20
    }
}

object EloPointsCalculator {
  def apply[F[_]](implicit ev: EloPointsCalculator[F]): EloPointsCalculator[F] = ev
  def impl[F[_]: Monad](gameManager: GameManager[F]): EloPointsCalculator[F] =
    new EloPointsCalculator[F](gameManager)
}
