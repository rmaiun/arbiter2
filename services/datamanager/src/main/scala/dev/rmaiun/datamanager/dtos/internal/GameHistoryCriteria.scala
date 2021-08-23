package dev.rmaiun.datamanager.dtos.internal

case class GameHistoryCriteria(
  realm: String,
  season: Option[String],
  shutout: Option[Boolean] = None,
  losers: List[String] = Nil,
  winners: List[String] = Nil
)
