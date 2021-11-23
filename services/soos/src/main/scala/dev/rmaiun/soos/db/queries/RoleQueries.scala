package dev.rmaiun.soos.db.queries

import cats.data.NonEmptyList
import dev.rmaiun.soos.db.entities.Role
import dev.rmaiun.soos.db.projections.UserRole
import doobie.Fragments
import doobie.implicits._

object RoleQueries {

  def getById(id: Long): doobie.Query0[Role] =
    sql"""
         | select id, value, permission
         | from role where role.id = $id limit 1
    """.stripMargin.query[Role]

  def getByValue(value: String): doobie.Query0[Role] =
    sql"""
         | select id, value, permission
         | from role where role.value = $value limit 1
    """.stripMargin.query[Role]

  def insert(role: Role): doobie.Update0 =
    sql"""
         | insert into role (id, value, permission)
         | values (${role.id}, ${role.value},${role.permission})
        """.stripMargin.update

  def update(role: Role): doobie.Update0 =
    sql"""
         | update role
         | set value=${role.value},
         |     permission=${role.permission}
         | where id = ${role.id}
    """.stripMargin.update

  def findUserRoleByRealm(user: String, realm: String): doobie.Query0[Role] =
    sql"""
         | select role.id, role.value, role.permission 
         | from user_realm_role
         | inner join realm on user_realm_role.realm = realm.id
         | inner join role on user_realm_role.role = role.id
         | inner join user on user_realm_role.user = user.id
         | where realm.name = $realm and user.surname = $user
    """.stripMargin.query[Role]

  def findUserRoleByRealm(userTid: Long, realm: String): doobie.Query0[Role] =
    sql"""
         | select role.id, role.value, role.permission
         | from user_realm_role
         | inner join realm on user_realm_role.realm = realm.id
         | inner join role on user_realm_role.role = role.id
         | inner join user on user_realm_role.user = user.id
         | where realm.name = $realm and user.tid = $userTid
    """.stripMargin.query[Role]

  def findAllUserRolesForRealm(realm: String): doobie.Query0[UserRole] =
    sql"""
         | select user.surname, user.tid, role.value as role
         | from user_realm_role
         | inner join realm on user_realm_role.realm = realm.id
         | inner join role on user_realm_role.role = role.id
         | inner join user on user_realm_role.user = user.id
         | where realm.name = $realm and user.active is true
    """.stripMargin.query[UserRole]

  def listAll: doobie.Query0[Role] =
    sql"select id, value, permission from role"
      .query[Role]

  def deleteByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"delete from role where " ++ Fragments.in(fr"role.id", ids)
        query.update
      case None =>
        sql"delete from role".update
    }
}
