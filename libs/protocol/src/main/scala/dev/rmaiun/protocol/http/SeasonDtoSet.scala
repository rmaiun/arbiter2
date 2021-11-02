package dev.rmaiun.protocol.http

import java.time.ZonedDateTime

object SeasonDtoSet {
  case class CreateSeasonDtoIn(
    id: Option[Long],
    name: String,
    algorithm: Option[String],
    realm: String,
    endNotification: Option[ZonedDateTime]
  )
  case class CreateSeasonDtoOut(id: Long, name: String)
}
