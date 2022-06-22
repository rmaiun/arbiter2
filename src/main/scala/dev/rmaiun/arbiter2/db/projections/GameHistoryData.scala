package dev.rmaiun.arbiter2.db.projections

import java.time.ZonedDateTime

case class GameHistoryData(
  id: Long,
  realm: String,
  season: String,
  winner1: String,
  winner2: String,
  loser1: String,
  loser2: String,
  shutout: Boolean,
  createdAt: ZonedDateTime
)
