package dev.rmaiun.datamanager.repositories

import cats.Monad
import dev.rmaiun.datamanager.db.entities.{ GameHistory, GamePoints }
import dev.rmaiun.datamanager.db.projections.{ GameHistoryData, GamePointsData }
import dev.rmaiun.datamanager.db.queries.GameQueries
import dev.rmaiun.datamanager.dtos.internal.{ GameHistoryCriteria, GamePointsCriteria }
import doobie.ConnectionIO

trait GameRepo[F[_]] {
  def createGamePoint(gp: GamePoints): ConnectionIO[GamePoints]
  def createGameHistory(gh: GameHistory): ConnectionIO[GameHistory]
  def removeNGamePoints(idList: List[Long] = Nil): ConnectionIO[Int]
  def removeNGameHistory(idList: List[Long] = Nil): ConnectionIO[Int]
  def listPointsByCriteria(criteria: GamePointsCriteria): ConnectionIO[List[GamePointsData]]
  def listHistoryByCriteria(criteria: GameHistoryCriteria): ConnectionIO[List[GameHistoryData]]
}

object GameRepo {
  def apply[F[_]](implicit ev: GameRepo[F]): GameRepo[F] = ev

  def impl[F[_]: Monad]: GameRepo[F] = new GameRepo[F] {
    override def createGamePoint(gp: GamePoints): ConnectionIO[GamePoints] =
      GameQueries
        .insertPoints(gp)
        .withUniqueGeneratedKeys[Long]("id")
        .map(id => gp.copy(id = id))

    override def createGameHistory(gh: GameHistory): ConnectionIO[GameHistory] =
      GameQueries
        .insertHistory(gh)
        .withUniqueGeneratedKeys[Long]("id")
        .map(id => gh.copy(id = id))

    override def removeNGamePoints(idList: List[Long]): ConnectionIO[Int] =
      GameQueries.deletePointsByIdList(idList).run

    override def removeNGameHistory(idList: List[Long]): ConnectionIO[Int] =
      GameQueries.deleteHistoryByIdList(idList).run

    override def listPointsByCriteria(criteria: GamePointsCriteria): ConnectionIO[List[GamePointsData]] =
      GameQueries.listPointsByCriteria(criteria).to[List]

    override def listHistoryByCriteria(criteria: GameHistoryCriteria): ConnectionIO[List[GameHistoryData]] =
      GameQueries.listHistoryByCriteria(criteria).to[List]
  }
}
