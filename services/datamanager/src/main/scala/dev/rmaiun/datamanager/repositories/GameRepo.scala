package dev.rmaiun.datamanager.repositories

import cats.Monad
import cats.data.NonEmptyList
import dev.rmaiun.datamanager.db.entities.{ EloPoints, GameHistory }
import dev.rmaiun.datamanager.db.projections.{ EloPointsData, GameHistoryData }
import dev.rmaiun.datamanager.db.queries.GameQueries
import dev.rmaiun.datamanager.dtos.internal.{ EloPointsCriteria, GameHistoryCriteria }
import doobie.ConnectionIO

trait GameRepo[F[_]] {
  def createEloPoint(ep: EloPoints): ConnectionIO[EloPoints]
  def createGameHistory(gh: GameHistory): ConnectionIO[GameHistory]
  def removeNEloPoints(idList: List[Long] = Nil): ConnectionIO[Int]
  def removeNGameHistory(idList: List[Long] = Nil): ConnectionIO[Int]
  def listEloPointsByCriteria(criteria: EloPointsCriteria): ConnectionIO[List[EloPointsData]]
  def listCalculatedPoints(surnames: Option[NonEmptyList[String]] = None): ConnectionIO[List[EloPointsData]]
  def listHistoryByCriteria(criteria: GameHistoryCriteria): ConnectionIO[List[GameHistoryData]]
}

object GameRepo {
  def apply[F[_]](implicit ev: GameRepo[F]): GameRepo[F] = ev

  def impl[F[_]: Monad]: GameRepo[F] = new GameRepo[F] {
    override def createEloPoint(ep: EloPoints): ConnectionIO[EloPoints] =
      GameQueries
        .insertPoints(ep)
        .withUniqueGeneratedKeys[Long]("id")
        .map(id => ep.copy(id = id))

    override def createGameHistory(gh: GameHistory): ConnectionIO[GameHistory] =
      GameQueries
        .insertHistory(gh)
        .withUniqueGeneratedKeys[Long]("id")
        .map(id => gh.copy(id = id))

    override def removeNEloPoints(idList: List[Long]): ConnectionIO[Int] =
      GameQueries.deletePointsByIdList(idList).run

    override def removeNGameHistory(idList: List[Long]): ConnectionIO[Int] =
      GameQueries.deleteHistoryByIdList(idList).run

    override def listEloPointsByCriteria(criteria: EloPointsCriteria): ConnectionIO[List[EloPointsData]] =
      GameQueries.listPointsByCriteria(criteria).to[List]

    override def listCalculatedPoints(surnames: Option[NonEmptyList[String]]): ConnectionIO[List[EloPointsData]] =
      GameQueries.listCalculatedPoints(surnames).to[List]

    override def listHistoryByCriteria(criteria: GameHistoryCriteria): ConnectionIO[List[GameHistoryData]] =
      GameQueries.listHistoryByCriteria(criteria).to[List]
  }
}
