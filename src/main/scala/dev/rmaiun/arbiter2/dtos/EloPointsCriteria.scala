package dev.rmaiun.arbiter2.dtos

import cats.data.NonEmptyList

case class EloPointsCriteria(players: Option[NonEmptyList[String]])
