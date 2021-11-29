package dev.rmaiun.protocol.http.codec

import dev.rmaiun.protocol.http.SeasonDtoSet.{
  CreateSeasonDtoIn,
  CreateSeasonDtoOut,
  FindSeasonWithoutNotificationDtoOut,
  SeasonDto
}
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

trait SeasonDtoCodec {
  implicit val CreateSeasonDtoInEncoder: Encoder[CreateSeasonDtoIn] = deriveEncoder[CreateSeasonDtoIn]
  implicit val CreateSeasonDtoInDecoder: Decoder[CreateSeasonDtoIn] = deriveDecoder[CreateSeasonDtoIn]

  implicit val CreateSeasonDtoOutEncoder: Encoder[CreateSeasonDtoOut] = deriveEncoder[CreateSeasonDtoOut]
  implicit val CreateSeasonDtoOutDecoder: Decoder[CreateSeasonDtoOut] = deriveDecoder[CreateSeasonDtoOut]

  implicit val FindSeasonWithoutNotificationDtoOutEncoder: Encoder[FindSeasonWithoutNotificationDtoOut] =
    deriveEncoder[FindSeasonWithoutNotificationDtoOut]
  implicit val FindSeasonWithoutNotificationDtoOutDecoder: Decoder[FindSeasonWithoutNotificationDtoOut] =
    deriveDecoder[FindSeasonWithoutNotificationDtoOut]
}
