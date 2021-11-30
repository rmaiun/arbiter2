package dev.rmaiun.mabel.dtos

sealed trait SeasonNotificationData

case class SeasonReady(season: String)    extends SeasonNotificationData
case class SeasonNotReady(season: String) extends SeasonNotificationData
case class SeasonAbsent()                 extends SeasonNotificationData
