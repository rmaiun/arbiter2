package dev.rmaiun.soos.repositories

import cats.Monad
import cats.data.NonEmptyList
import dev.rmaiun.soos.db.entities.{ EloPoints, GameHistory }
import dev.rmaiun.soos.db.projections.{ EloPointExtended, EloPointsData, GameHistoryData }
import dev.rmaiun.soos.db.queries.GameQueries
import dev.rmaiun.soos.db.{ PageInfo, PageResult, PagedItems }
import dev.rmaiun.soos.dtos.{ EloPointsCriteria, GameHistoryCriteria }
import doobie.ConnectionIO

trait GameRepo[F[_]] {
  def createEloPoint(ep: EloPoints): ConnectionIO[EloPoints]
  def createGameHistory(gh: GameHistory): ConnectionIO[GameHistory]
  def removeNEloPoints(idList: List[Long] = Nil): ConnectionIO[Int]
  def removeNGameHistory(idList: List[Long] = Nil): ConnectionIO[Int]
  def listEloPointsByCriteria(criteria: EloPointsCriteria): ConnectionIO[List[EloPointExtended]]
  def listCalculatedPoints(surnames: Option[NonEmptyList[String]] = None): ConnectionIO[List[EloPointsData]]
  def listHistoryByCriteria(criteria: GameHistoryCriteria): ConnectionIO[List[GameHistoryData]]
  def listEloPoints(pageInfo: PageInfo): ConnectionIO[PagedItems[EloPoints]]
  def listAllGameHistory(pageInfo: PageInfo): ConnectionIO[PagedItems[GameHistory]]
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

    override def listEloPointsByCriteria(criteria: EloPointsCriteria): ConnectionIO[List[EloPointExtended]] =
      GameQueries.listPointsByCriteria(criteria).to[List]

    override def listCalculatedPoints(surnames: Option[NonEmptyList[String]]): ConnectionIO[List[EloPointsData]] =
      GameQueries.listCalculatedPoints(surnames).to[List]

    override def listHistoryByCriteria(criteria: GameHistoryCriteria): ConnectionIO[List[GameHistoryData]] =
      GameQueries.listHistoryByCriteria(criteria).to[List]

    override def listEloPoints(pageInfo: PageInfo): ConnectionIO[PagedItems[EloPoints]] =
      for {
        count <- GameQueries.countPointRows.unique
        query <- GameQueries.listAllPoints(pageInfo).to[List]
      } yield PagedItems(query, PageResult(pageInfo.page, pageInfo.qty, count))

    override def listAllGameHistory(pageInfo: PageInfo): ConnectionIO[PagedItems[GameHistory]] =
      for {
        count <- GameQueries.countGameHistoryRows.unique
        query <- GameQueries.listsAllGameHistory(pageInfo).to[List]
      } yield PagedItems(query, PageResult(pageInfo.page, pageInfo.qty, count))
  }
}
