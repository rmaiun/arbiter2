package dev.rmaiun.datamanager.db.projections

import java.time.ZonedDateTime

case class GameHistoryData(
  realm: String,
  season: String,
  winner1: String,
  winner2: String,
  loser1: String,
  loser2: String,
  shutout: Boolean,
  createdAt: ZonedDateTime
)
