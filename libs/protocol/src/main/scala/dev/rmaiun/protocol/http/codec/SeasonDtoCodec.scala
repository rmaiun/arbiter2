package dev.rmaiun.protocol.http.codec

import dev.rmaiun.protocol.http.SeasonDtoSet._
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

trait SeasonDtoCodec extends SubDtoCodec {
  implicit val CreateSeasonDtoInCodec: Codec[CreateSeasonDtoIn] = deriveCodec[CreateSeasonDtoIn]

  implicit val CreateSeasonDtoOutCodec: Codec[CreateSeasonDtoOut] = deriveCodec[CreateSeasonDtoOut]

  implicit val FindSeasonWithoutNotificationDtoInCodec: Codec[FindSeasonWithoutNotificationDtoIn] =
    deriveCodec[FindSeasonWithoutNotificationDtoIn]

  implicit val FindSeasonWithoutNotificationDtoOutCodec: Codec[FindSeasonWithoutNotificationDtoOut] =
    deriveCodec[FindSeasonWithoutNotificationDtoOut]

  implicit val NotifySeasonDtoInCodec: Codec[NotifySeasonDtoIn] = deriveCodec[NotifySeasonDtoIn]

  implicit val NotifySeasonDtoOutCodec: Codec[NotifySeasonDtoOut] = deriveCodec[NotifySeasonDtoOut]

}
