package dev.rmaiun.mabel.db.projections

import java.time.ZonedDateTime

case class EloPointExtended(id: Long, player: String, points: Int, created: ZonedDateTime)
