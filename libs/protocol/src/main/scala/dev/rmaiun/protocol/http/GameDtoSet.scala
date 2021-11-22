package dev.rmaiun.protocol.http

import java.time.ZonedDateTime

object GameDtoSet {
  case class GameHistoryDtoIn(
    realm: String,
    season: String,
    w1: String,
    w2: String,
    l1: String,
    l2: String,
    shutout: Boolean = false,
    created: Option[ZonedDateTime]
  )

  case class GameHistoryDto(
    id: Long,
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

  case class EloPointsDto(user: String, value: Int, created: ZonedDateTime)
  case class CalculatedEloPointsDto(user: String, value: Int, gamesPlayed: Int)

  case class AddGameHistoryDtoIn(historyElement: GameHistoryDtoIn, moderatorTid: Long)
  case class AddGameHistoryDtoOut(storedRound: GameHistoryDto)

  case class AddEloPointsDtoIn(points: EloPointsDto, moderatorTid: Long, realm:String)
  case class AddEloPointsDtoOut(id: Long)

  case class ListGameHistoryDtoIn(realm: String, season: String)
  case class ListGameHistoryDtoOut(games: List[StoredGameHistoryDto])

  case class ListEloPointsDtoIn(users: Option[List[String]])
  case class ListEloPointsDtoOut(calculatedEloPoints: List[CalculatedEloPointsDto], unratedPlayers: List[String] = Nil)
}
