package dev.rmaiun.mabel.db.entities

import java.time.{ ZoneOffset, ZonedDateTime }

case class GameHistory(
  id: Long,
  realm: Long,
  season: Long,
  w1: Long,
  w2: Long,
  l1: Long,
  l2: Long,
  shutout: Boolean = false,
  createdAt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
)
