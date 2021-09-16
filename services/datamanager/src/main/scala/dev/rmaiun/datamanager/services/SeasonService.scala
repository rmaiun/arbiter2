package dev.rmaiun.datamanager.services

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.common.SeasonHelper
import dev.rmaiun.datamanager.db.entities.Season
import dev.rmaiun.datamanager.errors.SeasonErrors.SeasonNotFoundRuntimeException
import dev.rmaiun.datamanager.repositories.SeasonRepo
import dev.rmaiun.flowtypes.Flow.Flow
import doobie.hikari.HikariTransactor
import doobie.implicits._
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow

trait SeasonService[F[_]] {
  def findSeason(name: String, realm:String): Flow[F, Season]
  def storeSeason(name: String): Flow[F, Season]
  def updateSeason(s: Season): Flow[F, Season]
  def listSeasons(realm: Option[String] = None): Flow[F, List[Season]]
}

object SeasonService{
  def apply[F[_]](implicit ev: SeasonService[F]): SeasonService[F] = ev
  def impl[F[_]: Monad: Sync](seasonRepo: SeasonRepo[F], xa: HikariTransactor[F]): SeasonService[F] = new SeasonService[F] {
    override def findSeason(name: String, realm:String): Flow[F, Season] =
      findInternally(name,realm).flatMap{
        case Some(v) => Flow.pure(v)
        case None =>
          val error = SeasonNotFoundRuntimeException(Map("name" -> name))
          if (name == SeasonHelper.currentSeason){
            findInternally(SeasonHelper.currentSeason, realm)
              .leftFlatMap(Flow.error(error))
          }else{
            Flow.error(error)
          }
      }

    override def storeSeason(name: String): Flow[F, Season] = ???

    override def updateSeason(s: Season): Flow[F, Season] = ???

    override def listSeasons(realm: Option[String]): Flow[F, List[Season]] = ???

    private def findInternally(name:String, realm:String):Flow[F,Option[Season]] =
      seasonRepo.getBySeasonNameRealm(name, realm).transact(xa).attemptSql.adaptError
  }
}
