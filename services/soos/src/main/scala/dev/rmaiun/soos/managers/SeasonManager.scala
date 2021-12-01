package dev.rmaiun.soos.managers

import cats.Monad
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.protocol.http.SeasonDtoSet.{CreateSeasonDtoIn, CreateSeasonDtoOut, FindSeasonWithoutNotificationDtoOut}
import dev.rmaiun.soos.db.entities.Season
import dev.rmaiun.soos.helpers.ConfigProvider.AppConfig
import dev.rmaiun.soos.services.{AlgorithmService, RealmService, SeasonService}
import dev.rmaiun.soos.validations.SeasonValidationSet._
import dev.rmaiun.validation.Validator

trait SeasonManager[F[_]] {
  def createSeason(dtoIn: CreateSeasonDtoIn): Flow[F, CreateSeasonDtoOut]
  def listAllSeasons(): Flow[F, List[Season]]
  def findSeasonWithoutNotification():Flow[F, FindSeasonWithoutNotificationDtoOut]
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

    override def findSeasonWithoutNotification(): Flow[F, FindSeasonWithoutNotificationDtoOut] = {
      seasonService.
    }
  }
}
