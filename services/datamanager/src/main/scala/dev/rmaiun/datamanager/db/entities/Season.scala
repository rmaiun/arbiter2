package dev.rmaiun.datamanager.db.entities

import java.time.ZonedDateTime

case class Season(id: Long, name: String, algorithm: Long, endNotification: Option[ZonedDateTime] = None)
