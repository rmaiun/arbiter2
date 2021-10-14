package dev.rmaiun.protocol.http.codec

import dev.rmaiun.protocol.http.GameDtoSet.{AddEloPointsDtoIn, AddEloPointsDtoOut, ListEloPointsDtoIn, ListEloPointsDtoOut}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

trait EloPointsDtoCodec extends SubDtoCodec {
  implicit val AddEloPointsDtoInEncoder: Encoder[AddEloPointsDtoIn] = deriveEncoder[AddEloPointsDtoIn]
  implicit val AddEloPointsDtoInDecoder: Decoder[AddEloPointsDtoIn] = deriveDecoder[AddEloPointsDtoIn]

  implicit val AddEloPointsDtoOutEncoder: Encoder[AddEloPointsDtoOut] = deriveEncoder[AddEloPointsDtoOut]
  implicit val AddEloPointsDtoOutDecoder: Decoder[AddEloPointsDtoOut] = deriveDecoder[AddEloPointsDtoOut]


  implicit val ListEloPointsDtoInEncoder: Encoder[ListEloPointsDtoIn] = deriveEncoder[ListEloPointsDtoIn]
  implicit val ListEloPointsDtoInDecoder: Decoder[ListEloPointsDtoIn] = deriveDecoder[ListEloPointsDtoIn]

  implicit val ListEloPointsDtoOutEncoder: Encoder[ListEloPointsDtoOut] = deriveEncoder[ListEloPointsDtoOut]
  implicit val ListEloPointsDtoOutDecoder: Decoder[ListEloPointsDtoOut] = deriveDecoder[ListEloPointsDtoOut]

}
