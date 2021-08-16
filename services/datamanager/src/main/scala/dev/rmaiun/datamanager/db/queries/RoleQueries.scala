package dev.rmaiun.datamanager.db.queries

import cats.data.NonEmptyList
import dev.rmaiun.datamanager.db.entities.Role
import doobie.Fragments
import doobie.implicits._

object RoleQueries {

  def getById(id: Long): doobie.Query0[Role] =
    sql"""
         | select id, value, permission
         | from role where role.id = $id limit 1
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
