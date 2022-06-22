package dev.rmaiun.arbiter2.dtos.stats

import java.time.ZonedDateTime

case class StatsState(player: String, created: ZonedDateTime, score: Int)
object StatsState {
  def winner(player: String, created: ZonedDateTime): StatsState = StatsState(player, created, 1)
  def loser(player: String, created: ZonedDateTime): StatsState  = StatsState(player, created, -1)
}
