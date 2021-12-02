package dev.rmaiun.protocol.http.codec

import dev.rmaiun.protocol.http.SeasonDtoSet._
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

trait SeasonDtoCodec extends SubDtoCodec {
  implicit val CreateSeasonDtoInEncoder: Encoder[CreateSeasonDtoIn] = deriveEncoder[CreateSeasonDtoIn]
  implicit val CreateSeasonDtoInDecoder: Decoder[CreateSeasonDtoIn] = deriveDecoder[CreateSeasonDtoIn]

  implicit val CreateSeasonDtoOutEncoder: Encoder[CreateSeasonDtoOut] = deriveEncoder[CreateSeasonDtoOut]
  implicit val CreateSeasonDtoOutDecoder: Decoder[CreateSeasonDtoOut] = deriveDecoder[CreateSeasonDtoOut]

  implicit val FindSeasonWithoutNotificationDtoInEncoder: Encoder[FindSeasonWithoutNotificationDtoIn] =
    deriveEncoder[FindSeasonWithoutNotificationDtoIn]
  implicit val FindSeasonWithoutNotificationDtoInDecoder: Decoder[FindSeasonWithoutNotificationDtoIn] =
    deriveDecoder[FindSeasonWithoutNotificationDtoIn]

  implicit val FindSeasonWithoutNotificationDtoOutEncoder: Encoder[FindSeasonWithoutNotificationDtoOut] =
    deriveEncoder[FindSeasonWithoutNotificationDtoOut]
  implicit val FindSeasonWithoutNotificationDtoOutDecoder: Decoder[FindSeasonWithoutNotificationDtoOut] =
    deriveDecoder[FindSeasonWithoutNotificationDtoOut]

  implicit val NotifySeasonDtoInEncoder: Encoder[NotifySeasonDtoIn] = deriveEncoder[NotifySeasonDtoIn]
  implicit val NotifySeasonDtoInDecoder: Decoder[NotifySeasonDtoIn] = deriveDecoder[NotifySeasonDtoIn]

  implicit val NotifySeasonDtoOutEncoder: Encoder[NotifySeasonDtoOut] = deriveEncoder[NotifySeasonDtoOut]
  implicit val NotifySeasonDtoOutDecoder: Decoder[NotifySeasonDtoOut] = deriveDecoder[NotifySeasonDtoOut]

}
