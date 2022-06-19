package dev.rmaiun.mabel.commands

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import java.time.ZonedDateTime

case class AddRoundCmd(
  w1: String,
  w2: String,
  l1: String,
  l2: String,
  shutout: Boolean,
  moderator: Long,
  created: Option[ZonedDateTime] = None,
  season: Option[String] = None
)

object AddRoundCmd {
  implicit val AddRoundCmdCodec: Codec[AddRoundCmd] = deriveCodec[AddRoundCmd]
}
