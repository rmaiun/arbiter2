package dev.rmaiun.datamanager.db.entities

import java.time.ZonedDateTime

case class User(id: Long, name: String,
                nickname: Option[String],
                tid: Option[String],
                createdAt: ZonedDateTime,
                active: Boolean)
