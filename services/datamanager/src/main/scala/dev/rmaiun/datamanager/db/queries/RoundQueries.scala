package dev.rmaiun.datamanager.db.queries

import java.time.LocalDateTime

import com.mairo.ukl.domains.Round
import doobie.implicits._
import doobie.implicits.javatime._

object RoundQueries {

  def findAllRounds: doobie.Query0[Round] = {
    sql"SELECT * FROM round"
      .query[Round]
  }

  def limitedLastRoundsBySeason(seasonId: Long, num: Int): doobie.Query0[Round] = {
    sql"SELECT *  FROM round  r WHERE r.season_id = $seasonId ORDER BY created DESC LIMIT $num"
      .query[Round]
  }

  def listRoundsBySeason(seasonId: Long): doobie.Query0[Round] = {
    sql"SELECT *  FROM round  r WHERE r.season_id = $seasonId"
      .query[Round]
  }

  def deleteRoundById(id: Long): doobie.Update0 = {
    sql"DELETE FROM round where round.id = $id"
      .update
  }

  def clearTable: doobie.Update0 = {
    sql"DELETE FROM round"
      .update
  }


  def getRoundById(id: Long): doobie.Query0[Round] = {
    sql"SELECT * FROM round WHERE round.id = $id LIMIT 1"
      .query[Round]
  }

  def insertRound(winner1: Long, winner2: Long, loser1: Long, loser2: Long, shutout: Boolean, seasonId: Long, created: LocalDateTime): doobie.Update0 = {
    sql"""
         | INSERT into round (winner1_id, winner2_id, loser1_id, loser2_id, shutout, season_id, created)
         | VALUES ($winner1, $winner2, $loser1, $loser2, $shutout, $seasonId, $created )
      """.stripMargin
      .update
  }

  def updateRound(round: Round): doobie.Update0 = {
    sql"""
         |UPDATE round
         | SET winner1_id=${round.winner1},
         |  winner2_id = ${round.winner2},
         |  loser1_id = ${round.loser1},
         |  loser2_id = ${round.loser2},
         |  shutout = ${round.shutout},
         |  season_id = ${round.seasonId},
         |  created = ${round.created}
         | WHERE id = ${round.id}
    """.stripMargin
      .update
  }
}
