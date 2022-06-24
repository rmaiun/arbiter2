package dev.rmaiun.protocol.http.codec

import dev.rmaiun.protocol.http.GameDtoSet._
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

trait GameHistoryDtoCodec extends SubDtoCodec {
  implicit val GameHistoryDtoInCodec: Codec[GameHistoryDtoIn] = deriveCodec[GameHistoryDtoIn]

  implicit val StoredGameHistoryDtoCodec: Codec[StoredGameHistoryDto] = deriveCodec[StoredGameHistoryDto]

  implicit val AddGameHistoryDtoInCodec: Codec[AddGameHistoryDtoIn] = deriveCodec[AddGameHistoryDtoIn]

  implicit val AddGameHistoryDtoOutCodec: Codec[AddGameHistoryDtoOut] = deriveCodec[AddGameHistoryDtoOut]

  implicit val ListGameHistoryDtoInCodec: Codec[ListGameHistoryDtoIn] = deriveCodec[ListGameHistoryDtoIn]

  implicit val ListGameHistoryDtoOutCodec: Codec[ListGameHistoryDtoOut] = deriveCodec[ListGameHistoryDtoOut]

}
