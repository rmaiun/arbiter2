package dev.rmaiun.mabel.db.entities

case class UserRealmRole(realm: Long, user: Long, role: Long, botUsage: Boolean = false)
