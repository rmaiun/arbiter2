package dev.rmaiun.mabel.commands

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class AddPlayerCmd(surname: String, tid: Option[Long], admin: Boolean, moderator: Long)

object AddPlayerCmd {
  implicit val AddPlayerCmdDecoder: Decoder[AddPlayerCmd] = deriveDecoder[AddPlayerCmd]
  implicit val AddPlayerCmdEncoder: Encoder[AddPlayerCmd] = deriveEncoder[AddPlayerCmd]
}
