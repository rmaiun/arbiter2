package dev.rmaiun.datamanager.db.entities

import java.time.ZonedDateTime

case class GameHistory(id: Long,
                       realm: Long,
                       season: Long,
                       w1: Long,
                       w2: Long,
                       l1: Long,
                       l2: Long,
                       shutout: Boolean,
                       createdAt: ZonedDateTime)
