package dev.rmaiun.arbiter2.db.entities

case class UserRealmRole(realm: Long, user: Long, role: Long, botUsage: Boolean = false)
