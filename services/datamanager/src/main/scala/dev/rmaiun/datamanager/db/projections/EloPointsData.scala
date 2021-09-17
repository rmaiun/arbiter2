package dev.rmaiun.datamanager.db.projections

import java.time.ZonedDateTime

case class EloPointsData(id: Long, user: String, points: Int, created: ZonedDateTime)
