package dev.rmaiun.soos.db.entities

case class UserRealmRole(realm: Long, user: Long, role: Long, botUsage: Boolean = false)
