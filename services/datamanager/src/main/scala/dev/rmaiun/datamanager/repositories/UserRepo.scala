package dev.rmaiun.datamanager.repositories

import cats.Monad
import dev.rmaiun.datamanager.db.entities.User
import doobie.ConnectionIO

trait UserRepo[F[_]] {
  def getById(id: Long): ConnectionIO[Option[User]]
  def create(user: User): ConnectionIO[User]
  def update(user: User): ConnectionIO[User]
  def listAll: ConnectionIO[List[User]]
  def removeN(idList: List[Long] = Nil): ConnectionIO[Int]
  def findBySurnameAndRealm(surname: String, realm: String, active: Option[Boolean] = None): ConnectionIO[Option[User]]
  def findByNicknameAndRealm(
    nickname: String,
    realm: String,
    active: Option[Boolean] = None
  ): ConnectionIO[Option[User]]
  def findBySurnamesAndRealm(
    surnames: List[String],
    realm: String,
    active: Option[Boolean] = None
  ): ConnectionIO[Option[User]]
  def findByNicknamesAndRealm(
    nicknames: List[String],
    realm: String,
    active: Option[Boolean] = None
  ): ConnectionIO[Option[User]]
  def findLastStoredId: ConnectionIO[Long]
  def assignUserToRealm(userId: Long, realmId: Long, roleId: Long): ConnectionIO[Long]
}

object UserRepo {
  def apply[F[_]](implicit ev: UserRepo[F]): UserRepo[F] = ev

  def impl[F[_]: Monad]: UserRepo[F] = new UserRepo[F] {
    override def getById(id: Long): ConnectionIO[Option[User]] = ???

    override def create(user: User): ConnectionIO[User] = ???

    override def update(user: User): ConnectionIO[User] = ???

    override def listAll: ConnectionIO[List[User]] = ???

    override def removeN(idList: List[Long]): ConnectionIO[Int] = ???

    override def findBySurnameAndRealm(
      surname: String,
      realm: String,
      active: Option[Boolean]
    ): ConnectionIO[Option[User]] = ???

    override def findByNicknameAndRealm(
      nickname: String,
      realm: String,
      active: Option[Boolean]
    ): ConnectionIO[Option[User]] = ???

    override def findBySurnamesAndRealm(
      surnames: List[String],
      realm: String,
      active: Option[Boolean]
    ): ConnectionIO[Option[User]] = ???

    override def findByNicknamesAndRealm(
      nicknames: List[String],
      realm: String,
      active: Option[Boolean]
    ): ConnectionIO[Option[User]] = ???

    override def findLastStoredId: ConnectionIO[Long] = ???

    override def assignUserToRealm(userId: Long, realmId: Long, roleId: Long): ConnectionIO[Long] = ???
  }
}
