package dev.rmaiun.mabel.dtos

import cats.data.NonEmptyList

case class EloPointsCriteria(players: Option[NonEmptyList[String]])
