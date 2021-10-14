package dev.rmaiun.datamanager.dtos

import cats.data.NonEmptyList

case class EloPointsCriteria(players: Option[NonEmptyList[String]])
