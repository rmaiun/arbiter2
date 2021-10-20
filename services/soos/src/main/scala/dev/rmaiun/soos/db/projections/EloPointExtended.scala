package dev.rmaiun.soos.db.projections

import java.time.ZonedDateTime

case class EloPointExtended(id: Long, player: String, points: Int, created: ZonedDateTime)
