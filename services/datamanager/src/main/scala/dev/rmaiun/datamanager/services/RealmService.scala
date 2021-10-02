package dev.rmaiun.datamanager.services

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.datamanager.db.entities.Realm
import dev.rmaiun.datamanager.db.projections.RealmData
import dev.rmaiun.datamanager.errors.RealmErrors.RealmNotFoundRuntimeException
import dev.rmaiun.datamanager.repositories.RealmRepo
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.chrisdavenport.log4cats.Logger

trait RealmService[F[_]] {
  def update(realm: Realm): Flow[F, Realm]
  def create(realm: Realm): Flow[F, Realm]
  def get(id: Long): Flow[F, Realm]
  def getByName(name: String): Flow[F, Realm]
  def remove(idList: List[Long]): Flow[F, Int]
  def findByUser(surname: String): Flow[F, List[RealmData]]
}

object RealmService {
  def apply[F[_]](implicit ev: RealmService[F]): RealmService[F] = ev

  def impl[F[_]: Monad: Logger: Sync](
    realmRepo: RealmRepo[F],
    xa: HikariTransactor[F]
  ): RealmService[F] = new RealmService[F] {

    override def update(realm: Realm): Flow[F, Realm] =
      realmRepo.update(realm).transact(xa).attemptSql.adaptError

    override def create(realm: Realm): Flow[F, Realm] =
      realmRepo.create(realm).transact(xa).attemptSql.adaptError

    override def get(id: Long): Flow[F, Realm] =
      realmRepo.getById(id).transact(xa).attemptSql.adaptError.flatMap {
        case Some(value) => Flow.pure(value)
        case None        => Flow.error(RealmNotFoundRuntimeException(Map("id" -> s"$id")))
      }

    override def getByName(name: String): Flow[F, Realm] =
      realmRepo.getByName(name).transact(xa).attemptSql.adaptError.flatMap {
        case Some(value) => Flow.pure(value)
        case None        => Flow.error(RealmNotFoundRuntimeException(Map("name" -> s"$name")))
      }

    override def remove(idList: List[Long]): Flow[F, Int] =
      realmRepo.removeN(idList).transact(xa).attemptSql.adaptError

    override def findByUser(surname: String): Flow[F, List[RealmData]] =
      realmRepo.listRealmsByUser(surname).transact(xa).attemptSql.adaptError
  }
}
