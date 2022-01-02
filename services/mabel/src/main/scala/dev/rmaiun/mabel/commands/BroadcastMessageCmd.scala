package dev.rmaiun.mabel.commands

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

case class BroadcastMessageCmd(text: String, moderatorTid: Long, testMode: Boolean = false)

object BroadcastMessageCmd {
  implicit val BroadcastMessageCmdDecoder: Decoder[BroadcastMessageCmd] = deriveDecoder[BroadcastMessageCmd]
  implicit val BroadcastMessageCmdEncoder: Encoder[BroadcastMessageCmd] = deriveEncoder[BroadcastMessageCmd]
}
