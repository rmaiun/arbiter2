package dev.rmaiun.protocol.http.codec

import dev.rmaiun.protocol.http.GameDtoSet._
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import io.circe.{Codec, Decoder, Encoder}

trait EloPointsDtoCodec extends SubDtoCodec {
  implicit val AddEloPointsDtoInCodec: Codec[AddEloPointsDtoIn] = deriveCodec[AddEloPointsDtoIn]
  implicit val AddEloPointsDtoOutCodec: Codec[AddEloPointsDtoOut] = deriveCodec[AddEloPointsDtoOut]
  implicit val ListEloPointsDtoInCodec: Codec[ListEloPointsDtoIn] = deriveCodec[ListEloPointsDtoIn]
  implicit val ListEloPointsDtoOutCodec: Codec[ListEloPointsDtoOut] = deriveCodec[ListEloPointsDtoOut]

}
