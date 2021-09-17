package dev.rmaiun.datamanager.dtos.internal

import cats.data.NonEmptyList

case class EloPointsCriteria(players: Option[NonEmptyList[String]])
