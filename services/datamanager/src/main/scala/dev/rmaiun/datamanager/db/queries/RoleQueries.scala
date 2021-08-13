package dev.rmaiun.datamanager.db.queries

import cats.data.NonEmptyList
import dev.rmaiun.datamanager.db.entities.Role
import doobie.Fragments
import doobie.implicits._

object RoleQueries {

  def getById(id: Long): doobie.Query0[Role] =
    sql"""
         | SELECT id, value, permission
         | FROM role WHERE role.id = $id LIMIT 1
    """.stripMargin.query[Role]

  def insert(role: Role): doobie.Update0 =
    sql"""
         | INSERT into role (id, value, permission)
         | VALUES (${role.id}, ${role.value},${role.permission})
        """.stripMargin.update

  def update(role: Role): doobie.Update0 =
    sql"""
         | UPDATE role
         | SET value=${role.value},
         |     permission=${role.permission}
         | WHERE id = ${role.id}
    """.stripMargin.update

  def listAll: doobie.Query0[Role] =
    sql"SELECT id, value, permission FROM role"
      .query[Role]

  def deleteByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"DELETE FROM role where " ++ Fragments.in(fr"role.id", ids)
        query.update
      case None =>
        sql"DELETE FROM role".update
    }
}
