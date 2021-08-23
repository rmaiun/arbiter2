package dev.rmaiun.datamanager.db.entities

case class UserRealmRole(realm: Long, user: Long, role: Long, botUsage:Boolean = false)
