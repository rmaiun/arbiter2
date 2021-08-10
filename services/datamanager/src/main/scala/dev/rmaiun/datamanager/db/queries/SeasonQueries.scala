package dev.rmaiun.datamanager.db.queries

import cats.data.NonEmptyList
import dev.rmaiun.datamanager.db.entities.Season
import doobie.{ Fragments, Meta }
import doobie.implicits._
import doobie.implicits.javasql._

import java.sql.Timestamp
import java.time.{ ZoneOffset, ZonedDateTime }

object SeasonQueries {
  implicit val metaInstance: Meta[ZonedDateTime] = Meta[Timestamp]
    .imap(ts => ZonedDateTime.ofInstant(ts.toInstant, ZoneOffset.UTC))(zdt => Timestamp.from(zdt.toInstant))

  def getById(id: Long): doobie.Query0[Season] =
    sql"""
         | SELECT id, name, algorithm, end_notification as endNotification 
         | FROM season WHERE season.id = $id LIMIT 1
    """.stripMargin.query[Season]

  def insert(season: Season): doobie.Update0 =
    sql"""
         | INSERT into season (id, name,algorithm,end_notification)
         | VALUES (${season.id}, ${season.name},${season.algorithm}, ${season.endNotification})
        """.stripMargin.update

  def update(season: Season): doobie.Update0 =
    sql"""
         | UPDATE season
         | SET name=${season.name},
         |     algorithm=${season.algorithm},
         |     end_notification=${season.endNotification}
         | WHERE id = ${season.id}
    """.stripMargin.update

  def listAll: doobie.Query0[Season] =
    sql"SELECT id, name, algorithm, end_notification as endNotification FROM season"
      .query[Season]

  def deleteByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"DELETE FROM season where " ++ Fragments.in(fr"season.id", ids)
        query.update
      case None =>
        sql"DELETE FROM season".update
    }
}
