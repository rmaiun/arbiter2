package dev.rmaiun.arbiter2.dtos

import io.circe.{ Codec, Decoder }
import io.circe.generic.semiauto.deriveCodec

case class BotResponse(chatId: Long, msgId: Int, result: String)

object BotResponse {
  implicit val BotResponseCodec: Codec[BotResponse] = deriveCodec[BotResponse]
}
