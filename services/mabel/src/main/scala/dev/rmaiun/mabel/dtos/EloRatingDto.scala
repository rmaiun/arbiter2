package dev.rmaiun.mabel.dtos

object EloRatingDto {
  case class EloPlayers(w1: String, w2: String, l1: String, l2: String)
  case class CalculatedPoints(player: String, points: Int)
  case class UserCalculatedPoints(
    w1: CalculatedPoints,
    w2: CalculatedPoints,
    l1: CalculatedPoints,
    l2: CalculatedPoints
  )
}
