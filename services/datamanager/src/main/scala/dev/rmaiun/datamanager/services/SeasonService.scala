package dev.rmaiun.datamanager.services

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.common.SeasonHelper
import dev.rmaiun.datamanager.db.entities.{ Algorithm, Realm, Season }
import dev.rmaiun.datamanager.errors.SeasonErrors.SeasonNotFoundRuntimeException
import dev.rmaiun.datamanager.errors.UserErrors.SameUsersInRoundException
import dev.rmaiun.datamanager.helpers.ConfigProvider.Config
import dev.rmaiun.datamanager.repositories.SeasonRepo
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import doobie.hikari.HikariTransactor
import doobie.implicits._

trait SeasonService[F[_]] {
  def findSeason(name: String, realm: Realm): Flow[F, Season]
  def storeSeason(s: Season): Flow[F, Season]
  def updateSeason(s: Season): Flow[F, Season]
  def listSeasons(realms: Option[List[String]] = None, seasons: Option[List[String]] = None): Flow[F, List[Season]]
  def checkAllUsersAreDifferent(players: List[String]): Flow[F, Unit]
}

object SeasonService {
  def apply[F[_]](implicit ev: SeasonService[F]): SeasonService[F] = ev
  def impl[F[_]: Monad: Sync](
    seasonRepo: SeasonRepo[F],
    realmService: RealmService[F],
    algorithmService: AlgorithmService[F],
    xa: HikariTransactor[F]
  )(implicit cfg: Config): SeasonService[F] =
    new SeasonService[F] {
      override def findSeason(name: String, realm: Realm): Flow[F, Season] =
        for {
          alg <- algorithmService.getAlgorithmByName(cfg.app.defaultAlgorithm)
          s   <- findInternallyWithFallback(name, realm, alg)
        } yield s

      override def storeSeason(s: Season): Flow[F, Season] =
        seasonRepo.create(s).transact(xa).attemptSql.adaptError

      override def updateSeason(s: Season): Flow[F, Season] =
        seasonRepo.update(s).transact(xa).attemptSql.adaptError

      override def listSeasons(realm: Option[List[String]], seasons: Option[List[String]]): Flow[F, List[Season]] =
        seasonRepo.listAll().transact(xa).attemptSql.adaptError

      override def checkAllUsersAreDifferent(players: List[String]): Flow[F, Unit] =
        if (players.distinct.size != players.size) {
          Flow.error(SameUsersInRoundException(players))
        } else {
          Flow.unit
        }

      private def findInternally(name: String, realm: String): Flow[F, Option[Season]] =
        seasonRepo.getBySeasonNameRealm(name, realm).transact(xa).attemptSql.adaptError

      private def findInternallyWithFallback(season: String, realm: Realm, algorithm: Algorithm): Flow[F, Season] =
        findInternally(season, realm.name).flatMap {
          case Some(v) => Flow.pure(v)
          case None =>
            val error = SeasonNotFoundRuntimeException(Map("season" -> season))
            if (season == SeasonHelper.currentSeason) {
              storeSeason(Season(0, season, algorithm.id, realm.id))
            } else {
              Flow.error(error)
            }
        }
    }
}