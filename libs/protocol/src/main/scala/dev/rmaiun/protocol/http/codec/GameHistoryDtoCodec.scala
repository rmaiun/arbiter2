package dev.rmaiun.protocol.http.codec

import dev.rmaiun.protocol.http.GameDtoSet._
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

trait GameHistoryDtoCodec extends SubDtoCodec {
  implicit val GameHistoryDtoInEncoder: Encoder[GameHistoryDtoIn] = deriveEncoder[GameHistoryDtoIn]
  implicit val GameHistoryDtoInDecoder: Decoder[GameHistoryDtoIn] = deriveDecoder[GameHistoryDtoIn]

  implicit val StoredGameHistoryDtoEncoder: Encoder[StoredGameHistoryDto] = deriveEncoder[StoredGameHistoryDto]
  implicit val StoredGameHistoryDtoDecoder: Decoder[StoredGameHistoryDto] = deriveDecoder[StoredGameHistoryDto]

  implicit val AddGameHistoryDtoInEncoder: Encoder[AddGameHistoryDtoIn] = deriveEncoder[AddGameHistoryDtoIn]
  implicit val AddGameHistoryDtoInDecoder: Decoder[AddGameHistoryDtoIn] = deriveDecoder[AddGameHistoryDtoIn]

  implicit val AddGameHistoryDtoOutEncoder: Encoder[AddGameHistoryDtoOut] = deriveEncoder[AddGameHistoryDtoOut]
  implicit val AddGameHistoryDtoOutDecoder: Decoder[AddGameHistoryDtoOut] = deriveDecoder[AddGameHistoryDtoOut]

  implicit val ListGameHistoryDtoInEncoder: Encoder[ListGameHistoryDtoIn] = deriveEncoder[ListGameHistoryDtoIn]
  implicit val ListGameHistoryDtoInDecoder: Decoder[ListGameHistoryDtoIn] = deriveDecoder[ListGameHistoryDtoIn]

  implicit val ListGameHistoryDtoOutEncoder: Encoder[ListGameHistoryDtoOut] = deriveEncoder[ListGameHistoryDtoOut]
  implicit val ListGameHistoryDtoOutDecoder: Decoder[ListGameHistoryDtoOut] = deriveDecoder[ListGameHistoryDtoOut]

}
