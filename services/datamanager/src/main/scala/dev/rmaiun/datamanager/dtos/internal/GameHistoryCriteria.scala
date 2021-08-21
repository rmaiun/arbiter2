package dev.rmaiun.datamanager.dtos.internal

case class GameHistoryCriteria(
  realm: String,
  season: Option[String],
  winners: List[String] = Nil,
  losers: List[String] = Nil,
  shutout: Option[Boolean]
)
