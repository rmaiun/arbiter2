package dev.rmaiun.datamanager.dtos.internal

case class GamePointsCriteria(
  realm: String,
  season: Option[String],
  player: Option[String],
  shutout: Option[Boolean]
)
