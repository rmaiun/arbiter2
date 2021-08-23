package dev.rmaiun.datamanager.db.queries

import cats.data.NonEmptyList
import dev.rmaiun.datamanager.db.entities.{ GameHistory, GamePoints }
import dev.rmaiun.datamanager.db.projections.{ GameHistoryData, GamePointsData }
import dev.rmaiun.datamanager.dtos.internal.{ GameHistoryCriteria, GamePointsCriteria }
import doobie.Fragments
import doobie.implicits._

object GameQueries extends CustomMeta {

  def insertPoints(gp: GamePoints): doobie.Update0 =
    sql"""
         | insert into game_points(id, realm, season, user, points)
         | values (${gp.id}, ${gp.realm}, ${gp.season}, ${gp.user}, ${gp.points})
        """.stripMargin.update

  def insertHistory(gh: GameHistory): doobie.Update0 =
    sql"""
         | insert into game_history(id, realm, season, w1, w2, l1, l2, shutout, created_at)
         | values (${gh.id}, ${gh.realm}, ${gh.season}, ${gh.w1}, ${gh.w2}, ${gh.l1}, ${gh.l2}, ${gh.shutout}, ${gh.createdAt})
        """.stripMargin.update

  def listPointsByCriteria(c: GamePointsCriteria): doobie.Query0[GamePointsData] = {
    val baseWithRealmFragment = fr"""
                                    | select realm.name, season.name, user.surname, gp.points from game_points as gp
                                    | inner join realm on gp.realm = realm.id
                                    | inner join season on gp.season = season.id
                                    | inner join user on gp.user = user.id
                                    | where realm.name = ${c.realm}
                                  """.stripMargin
    val withSeason =
      c.season.fold(baseWithRealmFragment)(season => baseWithRealmFragment ++ fr" and season.name = $season")
    val withUser = c.player.fold(withSeason)(player => withSeason ++ fr" and user.surname = $player")
    withUser.query[GamePointsData]
  }

  def listHistoryByCriteria(c: GameHistoryCriteria): doobie.Query0[GameHistoryData] = {
    val baseWithRealmFragment = fr"""
                                    | select realm.name, season.name, u1.surname as winner1, u2.surname as winner2, u3.surname as loser1, u4.surname as loser2, gh.shutout, gh.created_at as createdAt
                                    | from game_history as gh
                                    | inner join realm on gh.realm = realm.id
                                    | inner join season on gh.season = season.id
                                    | inner join user as u1 on gh.w1 = user.id
                                    | inner join user as u2 on gh.w2 = user.id
                                    | inner join user as u3 on gh.l1 = user.id
                                    | inner join user as u4 on gh.l2 = user.id
                                    | where realm.name = ${c.realm}""".stripMargin
    val withSeason            = c.season.fold(baseWithRealmFragment)(season => baseWithRealmFragment ++ fr" and season.name = $season")
    val withShutout = c.shutout
      .map(flag => s"$flag")
      .fold(withSeason)(shutout => withSeason ++ fr" and gh.shutout is $shutout")
    withShutout.query[GameHistoryData]
  }

  def deletePointsByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"delete from game_points where " ++ Fragments.in(fr"game_points.id", ids)
        query.update
      case None =>
        sql"delete from game_points".update
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
