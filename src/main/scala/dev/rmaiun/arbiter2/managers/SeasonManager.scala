package dev.rmaiun.arbiter2.managers

import cats.Monad
import dev.rmaiun.arbiter2.db.entities.Season
import dev.rmaiun.arbiter2.helpers.ConfigProvider.AppConfig
import dev.rmaiun.arbiter2.services.{ AlgorithmService, RealmService, SeasonService }
import dev.rmaiun.common.DateFormatter
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.arbiter2.helpers.ConfigProvider.AppConfig
import dev.rmaiun.arbiter2.services.{ AlgorithmService, RealmService, SeasonService }
import dev.rmaiun.arbiter2.validations.SeasonValidationSet._
import dev.rmaiun.protocol.http.SeasonDtoSet._
import dev.rmaiun.validation.Validator

trait SeasonManager[F[_]] {
  def createSeason(dtoIn: CreateSeasonDtoIn): Flow[F, CreateSeasonDtoOut]
  def listAllSeasons(): Flow[F, List[Season]]
  def findSeasonWithoutNotification(
    dtoIn: FindSeasonWithoutNotificationDtoIn
  ): Flow[F, FindSeasonWithoutNotificationDtoOut]
  def notifySeason(dtoIn: NotifySeasonDtoIn): Flow[F, NotifySeasonDtoOut]
}
object SeasonManager {
  def apply[F[_]](implicit ev: UserManager[F]): UserManager[F] = ev

  def impl[F[_]: Monad](
    algorithmService: AlgorithmService[F],
    realmService: RealmService[F],
    seasonService: SeasonService[F],
    cfg: AppConfig
  ): SeasonManager[F] = new SeasonManager[F] {
    override def createSeason(dtoIn: CreateSeasonDtoIn): Flow[F, CreateSeasonDtoOut] =
      for {
        _     <- Validator.validateDto[F, CreateSeasonDtoIn](dtoIn)
        alg   <- algorithmService.getAlgorithmByName(dtoIn.algorithm.getOrElse(cfg.defaultAlgorithm))
        realm <- realmService.getByName(dtoIn.realm)
        season = Season(dtoIn.id.getOrElse(0), dtoIn.name, alg.id, realm.id, dtoIn.endNotification)
        s     <- seasonService.storeSeason(season)
      } yield CreateSeasonDtoOut(s.id, s.name)

    override def listAllSeasons(): Flow[F, List[Season]] =
      seasonService.listSeasons()

    override def findSeasonWithoutNotification(
      dtoIn: FindSeasonWithoutNotificationDtoIn
    ): Flow[F, FindSeasonWithoutNotificationDtoOut] =
      for {
        _      <- Validator.validateDto[F, FindSeasonWithoutNotificationDtoIn](dtoIn)
        realm  <- realmService.getByName(dtoIn.realm)
        season <- seasonService.findFirstSeasonWithoutNotification(realm.name)
      } yield {
        val seasonDto = season.map(s => SeasonDto(s.id, s.name))
        FindSeasonWithoutNotificationDtoOut(seasonDto)
      }

    override def notifySeason(dtoIn: NotifySeasonDtoIn): Flow[F, NotifySeasonDtoOut] = {
      val now = DateFormatter.now
      for {
        _      <- Validator.validateDto[F, NotifySeasonDtoIn](dtoIn)
        realm  <- realmService.getByName(dtoIn.realm)
        season <- seasonService.findSeason(dtoIn.season, realm)
        upd    <- seasonService.updateSeason(season.copy(endNotification = Some(now)))
      } yield NotifySeasonDtoOut(upd.name, now)
    }
  }
}
