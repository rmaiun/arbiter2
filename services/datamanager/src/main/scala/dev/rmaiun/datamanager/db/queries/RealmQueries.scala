package dev.rmaiun.datamanager.db.queries

import cats.data.NonEmptyList
import dev.rmaiun.datamanager.db.entities.Realm
import doobie.Fragments
import doobie.implicits._

object RealmQueries {

  def getById(id: Long): doobie.Query0[Realm] =
    sql"SELECT * FROM realm WHERE realm.id = $id LIMIT 1"
      .query[Realm]

  def insert(realm: Realm): doobie.Update0 =
    sql"""
         | INSERT into realm (id, name, team_size, selected_algorithm)
         | VALUES (${realm.id}, ${realm.name},${realm.teamSize},${realm.selectedAlgorithm})
        """.stripMargin.update

  def update(realm: Realm): doobie.Update0 =
    sql"""
         | UPDATE realm
         | SET name=${realm.name},
         |     team_size=${realm.teamSize},
         |     selected_algorithm=${realm.selectedAlgorithm}
         | WHERE id = ${realm.id}
    """.stripMargin.update

  def listAll: doobie.Query0[Realm] =
    sql"SELECT * FROM realm"
      .query[Realm]

  def deleteByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"DELETE FROM realm where " ++ Fragments.in(fr"realm.id", ids)
        query.update
      case None =>
        sql"DELETE FROM realm".update
    }
}
