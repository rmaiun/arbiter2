package dev.rmaiun.datamanager.db.queries

import cats.data.NonEmptyList
import dev.rmaiun.datamanager.db.entities.User
import doobie.Fragments
import doobie.implicits._
import doobie.util.fragment

object UserQueries extends CustomMeta {

  def getById(id: Long): doobie.Query0[User] =
    sql"""
         | select id, surname, nickname, tid, active, created_at as createdAt
         | from user where user.id = $id limit 1
    """.stripMargin.query[User]

  def getByTid(tid: Long): doobie.Query0[User] =
    sql"""
         | select id, surname, nickname, tid, active, created_at as createdAt
         | from user where user.tid = $tid limit 1
    """.stripMargin.query[User]

  def getActiveByName(name: String, active: Option[Boolean] = None): doobie.Query0[User] = {
    val baseQuery   = fr"""
                        | select id, surname, nickname, tid, active, created_at as createdAt
                        | from user
                        | where user.name = $name
                 """.stripMargin
    val resultQuery = activeBasedFragment(baseQuery, active) ++ limitFragment
    resultQuery.query[User]
  }

  def getActiveByNickname(nickname: String, active: Option[Boolean] = None): doobie.Query0[User] = {
    val baseQuery   = fr"""
                        | select id, surname, nickname, tid, active, created_at as createdAt
                        | from user
                        | where user.nickname = $nickname
                 """.stripMargin
    val resultQuery = activeBasedFragment(baseQuery, active) ++ limitFragment
    resultQuery.query[User]
  }

  def getActiveByNames(names: NonEmptyList[String], active: Option[Boolean] = None): doobie.Query0[User] = {
    val baseQuery   = fr"""
                        | select id, surname, nickname, tid, active, created_at as createdAt
                        | from user
                        | where
                 """.stripMargin ++ Fragments.in(fr"user.name", names)
    val resultQuery = activeBasedFragment(baseQuery, active)
    resultQuery.query[User]
  }

  def getActiveByNicknames(nicknames: NonEmptyList[String], active: Option[Boolean] = None): doobie.Query0[User] = {
    val baseQuery   = fr"""
                        | select id, surname, nickname, tid, active, created_at as createdAt
                        | from user
                        | where
                 """.stripMargin ++ Fragments.in(fr"user.nickname", nicknames)
    val resultQuery = activeBasedFragment(baseQuery, active)
    resultQuery.query[User]
  }

  def insert(user: User): doobie.Update0 =
    sql"""
         | insert into user (id, surname, nickname, tid, active, created_at)
         | values (${user.id}, ${user.surname}, ${user.nickname}, ${user.tid}, ${user.active}, ${user.createdAt})
        """.stripMargin.update

  def update(user: User): doobie.Update0 =
    sql"""
         | update user
         | set surname=${user.surname},
         |     nickname=${user.nickname},
         |     tid=${user.tid},
         |     active=${user.active},
         |     created_at=${user.createdAt}
         | where id = ${user.id}
    """.stripMargin.update

  def listAll: doobie.Query0[User] =
    sql"select id, surname, nickname, tid, active, created_at as createdAt from user"
      .query[User]

  def deleteByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"delete from user where " ++ Fragments.in(fr"user.id", ids)
        query.update
      case None =>
        sql"delete from user".update
    }

  private def activeBasedFragment(f: fragment.Fragment, active: Option[Boolean]): fragment.Fragment =
    active match {
      case Some(true) =>
        f ++ Fragments.and(fr"user.active = true")
      case Some(false) =>
        f ++ Fragments.and(fr"user.active = false")
      case None =>
        f
    }

  def limitFragment = fr"limit 1"
}
