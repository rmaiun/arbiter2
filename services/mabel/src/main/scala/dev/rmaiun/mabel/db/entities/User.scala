package dev.rmaiun.mabel.db.entities

import java.time.{ ZoneOffset, ZonedDateTime }

case class User(
  id: Long,
  surname: String,
  nickname: Option[String] = None,
  tid: Option[Long] = None,
  active: Boolean = true,
  createdAt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
)
