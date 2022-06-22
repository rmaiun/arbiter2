package dev.rmaiun.arbiter2.dtos.stats

import java.time.ZonedDateTime

case class TmpStreak(player: String, shutout: Int, upd: ZonedDateTime)
