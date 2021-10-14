package dev.rmaiun.mabel.commands

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class AddRoundCmd(w1: String,
                       w2: String,
                       l1: String,
                       l2: String,
                       shutout: Boolean,
                       moderator: Long)

object AddRoundCmd{
  implicit val AddRoundCmdDecoder: Decoder[AddRoundCmd] = deriveDecoder[AddRoundCmd]

}
