package dev.rmaiun.datamanager.repositories

import cats.Monad
import dev.rmaiun.datamanager.db.entities.Role
import dev.rmaiun.datamanager.db.queries.RoleQueries
import doobie.ConnectionIO

trait RoleRepo[F[_]] {
  def getById(id: Long): ConnectionIO[Option[Role]]
  def create(role: Role): ConnectionIO[Role]
  def update(role: Role): ConnectionIO[Role]
  def listAll: ConnectionIO[List[Role]]
  def removeN(idList: List[Long] = Nil): ConnectionIO[Int]
}
object RoleRepo {

  def apply[F[_]](implicit ev: RoleRepo[F]): RoleRepo[F] = ev

  def impl[F[_]: Monad]: RoleRepo[F] = new RoleRepo[F] {
    override def getById(id: Long): ConnectionIO[Option[Role]] = RoleQueries.getById(id).option

    override def create(role: Role): ConnectionIO[Role] = RoleQueries
      .insert(role)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => role.copy(id = id))

    override def update(role: Role): ConnectionIO[Role] = RoleQueries
      .update(role)
      .run
      .map(_ => role)

    override def listAll: ConnectionIO[List[Role]] = RoleQueries.listAll.to[List]

    override def removeN(idList: List[Long]): ConnectionIO[Int] = RoleQueries.deleteByIdList(idList).run
  }
}
