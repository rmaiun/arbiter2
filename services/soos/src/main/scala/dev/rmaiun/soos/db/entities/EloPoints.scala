package dev.rmaiun.soos.db.entities

import java.time.ZonedDateTime

case class EloPoints(id: Long, user: Long, points: Int, created: ZonedDateTime)
