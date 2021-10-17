package dev.rmaiun.mabel.commands

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

case class AddRoundCmd(w1: String, w2: String, l1: String, l2: String, shutout: Boolean, moderator: Long)

object AddRoundCmd {
  implicit val AddRoundCmdDecoder: Decoder[AddRoundCmd] = deriveDecoder[AddRoundCmd]
  implicit val AddRoundCmdEncoder: Encoder[AddRoundCmd] = deriveEncoder[AddRoundCmd]
}
