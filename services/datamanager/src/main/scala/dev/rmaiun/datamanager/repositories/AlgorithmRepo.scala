package dev.rmaiun.datamanager.repositories

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.datamanager.db.entities.Algorithm
import dev.rmaiun.datamanager.db.queries.AlgorithmQueries
import doobie.ConnectionIO

trait AlgorithmRepo[F[_]] {
  def getById(id: Long): ConnectionIO[Option[Algorithm]]
  def create(algorithm: Algorithm): ConnectionIO[Algorithm]
  def update(algorithm: Algorithm): ConnectionIO[Algorithm]
  def listAll: ConnectionIO[List[Algorithm]]
  def removeN(idList: List[Long] = Nil): ConnectionIO[Int]
}
object AlgorithmRepo {
  def apply[F[_]](implicit ev: AlgorithmRepo[F]): AlgorithmRepo[F] = ev

  def impl[F[_]: Sync: Monad]: AlgorithmRepo[F] = new AlgorithmRepo[F] {
    override def getById(id: Long): ConnectionIO[Option[Algorithm]] = AlgorithmQueries.getById(id).option

    override def create(algorithm: Algorithm): ConnectionIO[Algorithm] = AlgorithmQueries
      .insert(algorithm)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => algorithm.copy(id = id))

    override def update(algorithm: Algorithm): ConnectionIO[Algorithm] = AlgorithmQueries
      .update(algorithm)
      .run
      .map(_ => algorithm)

    override def listAll: ConnectionIO[List[Algorithm]] = AlgorithmQueries.listAll.to[List]

    override def removeN(idList: List[Long]): ConnectionIO[Int] = AlgorithmQueries.deleteByIdList(idList).run
  }
}

