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
         | select id, name, algorithm, realm, end_notification as endNotification 
         | from season where season.id = $id limit 1
    """.stripMargin.query[Season]

  def getByNameRealm(season: String, realm: String): doobie.Query0[Season] =
    sql"""
         | select s.id, s.name, s.algorithm, s.realm, s.end_notification
         | from realm r
         |         inner join season s on r.id = s.realm
         | where r.name = $realm
         |   and s.name = $season
         | limit 1
    """.stripMargin.query[Season]

  def insert(season: Season): doobie.Update0 =
    sql"""
         | insert into season (id, name, algorithm, realm, end_notification)
         | values (${season.id}, ${season.name}, ${season.algorithm}, ${season.realm}, ${season.endNotification})
        """.stripMargin.update

  def update(season: Season): doobie.Update0 =
    sql"""
         | update season
         | set name=${season.name},
         |     algorithm=${season.algorithm},
         |     realm=${season.realm},
         |     end_notification=${season.endNotification}
         | where id = ${season.id}
    """.stripMargin.update

  def listAll: doobie.Query0[Season] =
    sql"select id, name, algorithm, realm, end_notification as endNotification from season"
      .query[Season]

  def deleteByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"delete from season where " ++ Fragments.in(fr"season.id", ids)
        query.update
      case None =>
        sql"delete from season".update
    }
}
