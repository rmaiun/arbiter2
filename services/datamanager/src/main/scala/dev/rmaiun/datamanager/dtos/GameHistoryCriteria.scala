package dev.rmaiun.datamanager.dtos

case class GameHistoryCriteria(
  realm: String,
  season: Option[String],
  shutout: Option[Boolean] = None,
  losers: List[String] = Nil,
  winners: List[String] = Nil
)
