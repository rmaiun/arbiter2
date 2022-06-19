package dev.rmaiun.mabel.dtos

import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, Decoder, Json}

case class BotRequest(cmd: String, chatId: Long, tid: Long, user: String, data: Option[Json] = None)

object BotRequest {
  implicit val BotRequestCodec: Codec[BotRequest] = deriveCodec[BotRequest]
}
