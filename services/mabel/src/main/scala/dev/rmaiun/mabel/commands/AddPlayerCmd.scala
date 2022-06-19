package dev.rmaiun.mabel.commands

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class AddPlayerCmd(surname: String, tid: Option[Long], admin: Boolean, moderator: Long)

object AddPlayerCmd {
  implicit val AddPlayerCmdCodec: Codec[AddPlayerCmd] = deriveCodec[AddPlayerCmd]
}
