package dev.rmaiun.arbiter2.dtos

sealed trait SeasonNotificationData

case class SeasonReady(season: String)    extends SeasonNotificationData
case class SeasonNotReady(season: String) extends SeasonNotificationData
case class SeasonAbsent()                 extends SeasonNotificationData
