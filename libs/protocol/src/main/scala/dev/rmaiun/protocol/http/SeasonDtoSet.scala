package dev.rmaiun.protocol.http

import java.time.ZonedDateTime

object SeasonDtoSet {
  case class SeasonDto(id: Long, name: String)
  case class FindSeasonWithoutNotificationDtoIn(realm: String)
  case class FindSeasonWithoutNotificationDtoOut(season: Option[SeasonDto])
  case class CreateSeasonDtoIn(
    id: Option[Long],
    name: String,
    algorithm: Option[String],
    realm: String,
    endNotification: Option[ZonedDateTime]
  )
  case class CreateSeasonDtoOut(id: Long, name: String)

  case class NotifySeasonDtoIn(season: String, realm:String)
  case class NotifySeasonDtoOut(season: String, notified: ZonedDateTime)
}
