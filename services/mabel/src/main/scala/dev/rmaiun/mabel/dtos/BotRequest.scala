package dev.rmaiun.mabel.dtos

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder, Json }

case class BotRequest(cmd: String, chatId: Long, tid: Long, user: String, data: Option[Json] = None)

object BotRequest {
  implicit val BotRequestDecoder: Decoder[BotRequest] = deriveDecoder[BotRequest]
  implicit val BotRequestEncoder: Encoder[BotRequest] = deriveEncoder[BotRequest]

}
