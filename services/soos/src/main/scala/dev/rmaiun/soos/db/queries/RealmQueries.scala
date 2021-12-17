package dev.rmaiun.soos.db.queries

import cats.data.NonEmptyList
import dev.rmaiun.soos.db.entities.{ Realm, UserRealmRole }
import dev.rmaiun.soos.db.projections.RealmData
import doobie.Fragments
import doobie.implicits._

object RealmQueries {

  def getById(id: Long): doobie.Query0[Realm] =
    sql"select * from realm where realm.id = $id limit 1"
      .query[Realm]

  def getByName(name: String): doobie.Query0[Realm] =
    sql"select * from realm where realm.name = $name limit 1"
      .query[Realm]

  def insert(realm: Realm): doobie.Update0 =
    sql"""
         | insert into realm (id, name, team_size, selected_algorithm)
         | values (${realm.id}, ${realm.name},${realm.teamSize},${realm.selectedAlgorithm})
        """.stripMargin.update

  def update(realm: Realm): doobie.Update0 =
    sql"""
         | update realm
         | set name=${realm.name},
         |     team_size=${realm.teamSize},
         |     selected_algorithm=${realm.selectedAlgorithm}
         | where id = ${realm.id}
    """.stripMargin.update

  def listAll: doobie.Query0[Realm] =
    sql"select * from realm"
      .query[Realm]

  def listAllRealmsByUser(surname: String): doobie.Query0[RealmData] =
    sql"""
         | select realm.id, realm.name, realm.team_size, realm.selected_algorithm as selectedAlgorithm, role.value as role, urr.bot_usage as botUsage
         | from user_realm_role as urr
         | inner join realm on urr.realm = realm.id
         | inner join user on urr.user = user.id
         | inner join role on urr.role = role.id
         | where user.surname = $surname
    """.stripMargin.query[RealmData]

  def listAllUserRealmRoles: doobie.Query0[UserRealmRole] =
    sql"""
         | select  realm, user, role, bot_usage as botUsage
         | from user_realm_role
    """.stripMargin.query[UserRealmRole]

  def deleteByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"delete from realm where " ++ Fragments.in(fr"realm.id", ids)
        query.update
      case None =>
        sql"delete from realm".update
    }
}
