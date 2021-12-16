package dev.rmaiun.soos.db.queries

import cats.data.NonEmptyList
import dev.rmaiun.soos.db.PageInfo
import dev.rmaiun.soos.db.entities.{ EloPoints, GameHistory }
import dev.rmaiun.soos.db.projections.{ EloPointExtended, EloPointsData, GameHistoryData }
import dev.rmaiun.soos.dtos.{ EloPointsCriteria, GameHistoryCriteria }
import doobie.Fragments
import doobie.implicits._

object GameQueries extends CustomMeta {

  def insertPoints(ep: EloPoints): doobie.Update0 =
    sql"""
         | insert into elo_points(id, user, points, created)
         | values (${ep.id}, ${ep.user}, ${ep.points}, ${ep.created})
        """.stripMargin.update

  def insertHistory(gh: GameHistory): doobie.Update0 =
    sql"""
         | insert into game_history(id, realm, season, w1, w2, l1, l2, shutout, created_at)
         | values (${gh.id}, ${gh.realm}, ${gh.season}, ${gh.w1}, ${gh.w2}, ${gh.l1}, ${gh.l2}, ${gh.shutout}, ${gh.createdAt})
        """.stripMargin.update

  def listPointsByCriteria(c: EloPointsCriteria): doobie.Query0[EloPointExtended] = {
    val baseWithRealmFragment = fr"""
                                    | select ep.id, user.surname as player, ep.points, ep.created
                                    |  from elo_points as ep
                                    | inner join user on ep.user = user.id
                                  """.stripMargin
    val withUser =
      c.players.fold(baseWithRealmFragment)(players =>
        baseWithRealmFragment ++ fr"where" ++ Fragments.in(fr"user.surname", players)
      )
    withUser.query[EloPointExtended]
  }

  def listAllPoints(pageInfo: PageInfo): doobie.Query0[EloPoints] = {
    val skipElements = pageInfo.page * pageInfo.qty
    val fragment     = fr"""
                       | select ep.id, ep.user, ep.points, ep.created
                       |  from elo_points as ep
                       |  limit $skipElements, ${pageInfo.qty}
                                  """.stripMargin
    fragment.query[EloPoints]
  }
  def countPointRows: doobie.Query0[Int] =
    sql"select count(id) from arbiter.elo_points".query[Int]

  def listCalculatedPoints(players: Option[NonEmptyList[String]]): doobie.Query0[EloPointsData] = {
    val baseWithRealmFragment = fr"""
                                    | select u.surname, sum(ep.points), count(u.surname)
                                    |from elo_points as ep
                                    |         inner join user as u on ep.user = u.id
                                    |group by u.surname
                                  """.stripMargin
    val withUser =
      players.fold(baseWithRealmFragment)(players =>
        baseWithRealmFragment ++ fr"having" ++ Fragments.in(fr"u.surname", players)
      )
    withUser.query[EloPointsData]
  }

  def listHistoryByCriteria(c: GameHistoryCriteria): doobie.Query0[GameHistoryData] = {
    val baseWithRealmFragment = fr"""
                                    | select gh.id, realm.name, season.name, u1.surname as winner1, u2.surname as winner2, u3.surname as loser1, u4.surname as loser2, gh.shutout, gh.created_at as createdAt
                                    | from game_history as gh
                                    | inner join realm on gh.realm = realm.id
                                    | inner join season on gh.season = season.id
                                    | inner join user as u1 on gh.w1 = u1.id
                                    | inner join user as u2 on gh.w2 = u2.id
                                    | inner join user as u3 on gh.l1 = u3.id
                                    | inner join user as u4 on gh.l2 = u4.id
                                    | where realm.name = ${c.realm}""".stripMargin
    val withSeason =
      c.season.fold(baseWithRealmFragment)(season => baseWithRealmFragment ++ fr" and season.name = $season")
    val withShutout = c.shutout
      .map(flag => if (flag) "true" else "false")
      .fold(withSeason) { shutout =>
        val shutoutStr = "and gh.shutout is " + shutout
        withSeason ++ fr"$shutoutStr"
      }
    withShutout.query[GameHistoryData]
  }

  def listsAllHistory(realm: Long, season: Long): doobie.Query0[GameHistory] = {
    val fragment = fr"""
                       | select gh.id, gh.realm, gh.season, gh.w1, gh.w2, gh.l1, gh.l2, gh.shutout, gh.created_at as createdAt
                       | from game_history as gh
                       | where gh.realm = $realm and gh.season = $season""".stripMargin
    fragment.query[GameHistory]
  }

  def deletePointsByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"delete from elo_points where " ++ Fragments.in(fr"elo_points.id", ids)
        query.update
      case None =>
        sql"delete from elo_points".update
    }

  def deleteHistoryByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"delete from game_history where " ++ Fragments.in(fr"game_history.id", ids)
        query.update
      case None =>
        sql"delete from game_history".update
    }
}
