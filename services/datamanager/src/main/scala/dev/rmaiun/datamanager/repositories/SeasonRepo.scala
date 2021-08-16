package dev.rmaiun.datamanager.repositories

import cats.Monad
import dev.rmaiun.datamanager.db.entities.Season
import dev.rmaiun.datamanager.db.queries.SeasonQueries
import doobie.ConnectionIO

trait SeasonRepo[F[_]] {
  def getById(id: Long): ConnectionIO[Option[Season]]
  def getBySeasonNameRealm(name: String, realm: String): ConnectionIO[Option[Season]]
  def create(season: Season): ConnectionIO[Season]
  def update(season: Season): ConnectionIO[Season]
  def listAll: ConnectionIO[List[Season]]
  def removeN(idList: List[Long] = Nil): ConnectionIO[Int]
}
object SeasonRepo {
  def apply[F[_]](implicit ev: SeasonRepo[F]): SeasonRepo[F] = ev

  def impl[F[_]: Monad]: SeasonRepo[F] = new SeasonRepo[F] {
    override def getById(id: Long): ConnectionIO[Option[Season]] = SeasonQueries.getById(id).option

    override def getBySeasonNameRealm(name: String, realm: String): ConnectionIO[Option[Season]] =
      SeasonQueries.getByNameRealm(name, realm).option

    override def create(season: Season): ConnectionIO[Season] = SeasonQueries
      .insert(season)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => season.copy(id = id))

    override def update(season: Season): ConnectionIO[Season] = SeasonQueries
      .update(season)
      .run
      .map(_ => season)

    override def listAll: ConnectionIO[List[Season]] = SeasonQueries.listAll.to[List]

    override def removeN(idList: List[Long]): ConnectionIO[Int] = SeasonQueries.deleteByIdList(idList).run
  }
}
