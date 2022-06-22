package dev.rmaiun.arbiter2.commands

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class BroadcastMessageCmd(text: String, moderatorTid: Long, testMode: Boolean = false)

object BroadcastMessageCmd {
  implicit val BroadcastMessageCmdCodec: Codec[BroadcastMessageCmd] = deriveCodec[BroadcastMessageCmd]
}
