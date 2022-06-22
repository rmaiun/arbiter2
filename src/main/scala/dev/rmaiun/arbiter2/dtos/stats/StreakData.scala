package dev.rmaiun.arbiter2.dtos.stats

import dev.rmaiun.common.DateFormatter

import java.time.ZonedDateTime

case class StreakData(
  curW: Int,
  w: Int,
  curL: Int,
  l: Int,
  max: ZonedDateTime,
  min: ZonedDateTime
)

object StreakData {
  def default: StreakData = {
    val date = DateFormatter.now.minusYears(100)
    StreakData(0, 0, 0, 0, date, date)
  }
}
