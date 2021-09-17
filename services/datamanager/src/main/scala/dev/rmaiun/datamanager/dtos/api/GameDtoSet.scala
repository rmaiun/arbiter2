package dev.rmaiun.datamanager.dtos.api

import java.time.ZonedDateTime

object GameDtoSet {
  case class GameHistoryDto(
    realm: String,
    season: String,
    w1: String,
    w2: String,
    l1: String,
    l2: String,
    shutout: Boolean = false
  )
  case class StoredGameHistoryDto(
    realm: String,
    season: String,
    w1: String,
    w2: String,
    l1: String,
    l2: String,
    shutout: Boolean = false,
    createdAt: ZonedDateTime
  )

  case class EloPointsDto(user: String, value: Int, stored: ZonedDateTime)

  case class AddGameHistoryDtoIn(historyElement: GameHistoryDto, moderatorTid: Long)
  case class AddGameHistoryDtoOut(storedRound: GameHistoryDto)

  case class AddEloPointsDtoIn(points: EloPointsDto, moderatorTid: Long)
  case class AddEloPointsDtoOut(id: Long)

  case class ListGameHistoryDtoIn(realm: String, season: String)
  case class ListGameHistoryDtoOut(games: List[StoredGameHistoryDto])

  case class ListEloPointsDtoIn(users: Option[List[String]])
  case class ListEloPointsDtoOut(calculatedEloPoints: List[EloPointsDto], unratedPlayers: List[String] = Nil)
}
