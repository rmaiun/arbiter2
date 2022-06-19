package dev.rmaiun.mabel.repositories

import cats.Monad
import cats.data.NonEmptyList
import dev.rmaiun.mabel.db.entities.{ User, UserRealmRole }
import dev.rmaiun.mabel.db.queries.UserQueries
import doobie.ConnectionIO

trait UserRepo[F[_]] {
  def getById(id: Long): ConnectionIO[Option[User]]
  def create(user: User): ConnectionIO[User]
  def update(user: User): ConnectionIO[User]
  def listAll(realm: String, surnames: List[String] = Nil, active: Option[Boolean] = None): ConnectionIO[List[User]]
  def removeN(idList: List[Long] = Nil): ConnectionIO[Int]
  def findBySurname(surname: String, active: Option[Boolean] = None): ConnectionIO[Option[User]]
  def findByNickname(
    nickname: String,
    active: Option[Boolean] = None
  ): ConnectionIO[Option[User]]
  def findByTid(tid: Long): ConnectionIO[Option[User]]
  def findBySurnames(
    surnames: NonEmptyList[String],
    active: Option[Boolean] = None
  ): ConnectionIO[List[User]]
  def findByNicknames(
    nicknames: NonEmptyList[String],
    active: Option[Boolean] = None
  ): ConnectionIO[List[User]]
  def findAvailableId: ConnectionIO[Long]
  def assignUserToRealm(userRealmRole: UserRealmRole): ConnectionIO[Int]
  def clearUserRealmRoles: ConnectionIO[Int]
}

object UserRepo {
  def apply[F[_]](implicit ev: UserRepo[F]): UserRepo[F] = ev

  def impl[F[_]: Monad]: UserRepo[F] = new UserRepo[F] {
    override def getById(id: Long): ConnectionIO[Option[User]] =
      UserQueries.getById(id).option

    override def create(user: User): ConnectionIO[User] =
      UserQueries
        .insert(user)
        .withUniqueGeneratedKeys[Long]("id")
        .map(id => user.copy(id = id))

    override def update(user: User): ConnectionIO[User] =
      UserQueries
        .update(user)
        .run
        .map(_ => user)

    override def listAll(realm: String, surnames: List[String], active: Option[Boolean]): ConnectionIO[List[User]] =
      UserQueries.listAll(realm, surnames, active).to[List]

    override def removeN(idList: List[Long]): ConnectionIO[Int] =
      UserQueries.deleteByIdList(idList).run

    override def findBySurname(
      surname: String,
      active: Option[Boolean]
    ): ConnectionIO[Option[User]] =
      UserQueries.getActiveBySurname(surname, active).option

    override def findByNickname(
      nickname: String,
      active: Option[Boolean]
    ): ConnectionIO[Option[User]] =
      UserQueries.getActiveByNickname(nickname, active).option

    override def findByTid(tid: Long): ConnectionIO[Option[User]] =
      UserQueries.getByTid(tid).option

    override def findBySurnames(
      surnames: NonEmptyList[String],
      active: Option[Boolean]
    ): ConnectionIO[List[User]] =
      UserQueries.getActiveBySurnames(surnames, active).to[List]

    override def findByNicknames(
      nicknames: NonEmptyList[String],
      active: Option[Boolean]
    ): ConnectionIO[List[User]] =
      UserQueries.getActiveByNicknames(nicknames, active).to[List]

    override def findAvailableId: ConnectionIO[Long] =
      UserQueries.countUsers.map(a => a + 1L)

    override def assignUserToRealm(userRealmRole: UserRealmRole): ConnectionIO[Int] =
      UserQueries.insertUserRealmRole(userRealmRole).run

    override def clearUserRealmRoles: ConnectionIO[Int] =
      UserQueries.clearUserRealmRoles.run
  }
}
