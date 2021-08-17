package dev.rmaiun.datamanager.db.queries

import cats.data.NonEmptyList
import dev.rmaiun.datamanager.db.entities.{User, UserRealmRole}
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

  def getActiveBySurname(surname: String, active: Option[Boolean] = None): doobie.Query0[User] = {
    val baseQuery   = fr"""
                        | select id, surname, nickname, tid, active, created_at as createdAt
                        | from user
                        | where user.surname = $surname
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

  def getActiveBySurnames(surnames: NonEmptyList[String], active: Option[Boolean] = None): doobie.Query0[User] = {
    val baseQuery   = fr"""
                        | select id, surname, nickname, tid, active, created_at as createdAt
                        | from user
                        | where
                 """.stripMargin ++ Fragments.in(fr"user.surname", surnames)
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

  def listAll(realm: String, surnames: List[String], active: Option[Boolean]): doobie.Query0[User] = {
    val baseFragment           = fr"""
                           | select user.id, user.surname, user.nickname, user.tid,user.active, user.created_at as createdAt
                           | from user
                           | inner join user_realm_role as urr on user.id = urr.user
                           | inner join realm on urr.realm = realm.id
                           | where realm.name = $realm
    """.stripMargin
    val baseWithActiveFragment = activeBasedFragment(baseFragment, active)
    val orderFragment          = fr"order by user.id"
    if (surnames.isEmpty) {
      (baseWithActiveFragment ++ orderFragment).query[User]
    } else {
      val data                   = NonEmptyList(surnames.head, Nil)
      val activeSurnamesFragment = baseWithActiveFragment ++ fr"and" ++ Fragments.in(fr"user.surname", data)
      (activeSurnamesFragment ++ orderFragment).query[User]
    }
  }

  def countUsers: doobie.ConnectionIO[Int] = sql"select count(id) from user".query[Int].unique

  def insertUserRealmRole(urr: UserRealmRole): doobie.Update0 =
    sql"""
         | insert into user_realm_role
         | value (${urr.user}, ${urr.realm}, ${urr.role})
         """.stripMargin.update

  def deleteByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"delete from user where " ++ Fragments.in(fr"user.id", ids)
        query.update
      case None =>
        sql"delete from user".update
    }

  def clearUserRealmRoles: doobie.Update0 = sql"delete from user_realm_role".update

  private def activeBasedFragment(f: fragment.Fragment, active: Option[Boolean]): fragment.Fragment =
    active match {
      case Some(true) =>
        f ++ fr"and user.active is true"
      case Some(false) =>
        f ++ fr"and user.active is false"
      case None =>
        f
    }

  def limitFragment = fr"limit 1"
}
