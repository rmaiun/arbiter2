package dev.rmaiun.mabel.dtos

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

case class BotResponse(chatId: Long, msgId: Int, result: String)

object BotResponse {
  implicit val BotResponseDecoder: Decoder[BotResponse] = deriveDecoder[BotResponse]
  implicit val BotResponseEncoder: Encoder[BotResponse] = deriveEncoder[BotResponse]
}
