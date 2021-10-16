package dev.rmaiun.soos.dtos

import cats.data.NonEmptyList

case class EloPointsCriteria(players: Option[NonEmptyList[String]])
