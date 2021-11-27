package dev.rmaiun.mabel.commands

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

import java.time.ZonedDateTime

case class AddRoundCmd(
  w1: String,
  w2: String,
  l1: String,
  l2: String,
  shutout: Boolean,
  moderator: Long,
  created: Option[ZonedDateTime] = None,
  season: Option[String] =  None
)

object AddRoundCmd {
  implicit val AddRoundCmdDecoder: Decoder[AddRoundCmd] = deriveDecoder[AddRoundCmd]
  implicit val AddRoundCmdEncoder: Encoder[AddRoundCmd] = deriveEncoder[AddRoundCmd]
}
