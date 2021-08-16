package dev.rmaiun.datamanager.db.entities

import java.time.ZonedDateTime

case class Season(id: Long, name: String, algorithm: Long, realm: Long, endNotification: Option[ZonedDateTime] = None)
