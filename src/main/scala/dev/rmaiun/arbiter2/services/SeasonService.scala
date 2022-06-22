package dev.rmaiun.arbiter2.services

import dev.rmaiun.arbiter2.db.entities.{ Algorithm, Realm, Season }
import dev.rmaiun.arbiter2.helpers.ConfigProvider.Config
import dev.rmaiun.arbiter2.repositories.SeasonRepo
import dev.rmaiun.common.SeasonHelper
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.arbiter2.helpers.ConfigProvider.Config
import doobie.hikari.HikariTransactor
import doobie.implicits._
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.arbiter2.errors.SeasonErrors.SeasonNotFoundRuntimeException
import dev.rmaiun.arbiter2.errors.UserErrors.SameUsersInRoundException

trait SeasonService[F[_]] {
  def findSeason(name: String, realm: Realm): Flow[F, Season]
  def storeSeason(s: Season): Flow[F, Season]
  def updateSeason(s: Season): Flow[F, Season]
  def listSeasons(realms: Option[List[String]] = None, seasons: Option[List[String]] = None): Flow[F, List[Season]]
  def checkAllUsersAreDifferent(players: List[String]): Flow[F, Unit]
  def findFirstSeasonWithoutNotification(realm: String): Flow[F, Option[Season]]
  def remove(idList: List[Long] = Nil): Flow[F, Int]
}

object SeasonService {
  def apply[F[_]](implicit ev: SeasonService[F]): SeasonService[F] = ev
  def impl[F[_]: MonadThrowable](
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

      override def findFirstSeasonWithoutNotification(realm: String): Flow[F, Option[Season]] =
        seasonRepo.getFirstSeasonWithoutNotification(realm).transact(xa).attemptSql.adaptError

      override def remove(idList: List[Long]): Flow[F, Int] =
        seasonRepo.removeN(idList).transact(xa).attemptSql.adaptError

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
