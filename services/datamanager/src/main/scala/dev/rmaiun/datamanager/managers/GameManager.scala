package dev.rmaiun.datamanager.managers

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.datamanager.db.entities.GameHistory
import dev.rmaiun.datamanager.dtos.api.GameDtoSet._
import dev.rmaiun.datamanager.services.{GameService, RealmService, UserRightsService, UserService}
import dev.rmaiun.datamanager.validations.GameValidationSet._
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.validation.Validator

import java.time.{ZoneOffset, ZonedDateTime}

trait GameManager[F[_]] {
  def storeGameHistory(dtoIn: AddGameHistoryDtoIn): Flow[F, AddGameHistoryDtoOut]
  def listGameHistory(dtoIn: ListGameHistoryDtoIn): Flow[F, ListGameHistoryDtoOut]
  def addEloPoints(dtoIn: AddEloPointsDtoIn): Flow[F, AddEloPointsDtoOut]
  def listEloPoints(dtoIn: ListEloPointsDtoIn): Flow[F, ListEloPointsDtoOut]
}
object GameManager {
  def apply[F[_]](implicit ev: GameManager[F]): GameManager[F] = ev

  def impl[F[_]: Monad: Sync](gameService: GameService[F],
                              userService:UserService[F],
                              realmService: RealmService[F],
                              seasonService: SeasonService[F],
                              userRightsService: UserRightsService[F]): GameManager[F] = new GameManager[F] {
    override def storeGameHistory(dtoIn: AddGameHistoryDtoIn): Flow[F, AddGameHistoryDtoOut] = {
      for{
        _ <- Validator.validateDto[F, AddGameHistoryDtoIn](dtoIn)
        _ <- userRightsService.isUserPrivileged(dtoIn.moderatorTid)
        gh <- GameHistory(0,dtoIn.historyElement.realm, dtoIn.historyElement.season, dtoIn.historyElement.w1,dtoIn.historyElement.w2, dtoIn.historyElement.l1, dtoIn.historyElement.l2,dtoIn.historyElement.shutout,ZonedDateTime.now(ZoneOffset.UTC))
        res <- gameService.createGameHistory()
      }yield
    }
    }

    override def listGameHistory(dtoIn: ListGameHistoryDtoIn): Flow[F, ListGameHistoryDtoOut] = ???

    override def addEloPoints(dtoIn: AddEloPointsDtoIn): Flow[F, AddEloPointsDtoOut] = ???

    override def listEloPoints(dtoIn: ListEloPointsDtoIn): Flow[F, ListEloPointsDtoOut] = ???
  }
