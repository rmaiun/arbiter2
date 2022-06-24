package dev.rmaiun.protocol.http.codec

import dev.rmaiun.protocol.http.RealmDtoSet._
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

trait RealmDtoCodec extends SubDtoCodec {
  implicit val RegisterRealmDtoInCodec: Codec[RegisterRealmDtoIn] = deriveCodec[RegisterRealmDtoIn]

  implicit val RegisterRealmDtoOutCodec: Codec[RegisterRealmDtoOut] = deriveCodec[RegisterRealmDtoOut]

  implicit val UpdateRealmAlgorithmDtoInCodec: Codec[UpdateRealmAlgorithmDtoIn] =
    deriveCodec[UpdateRealmAlgorithmDtoIn]

  implicit val UpdateRealmAlgorithmDtoOutCodec: Codec[UpdateRealmAlgorithmDtoOut] =
    deriveCodec[UpdateRealmAlgorithmDtoOut]

  implicit val GetRealmDtoInCodec: Codec[GetRealmDtoIn] = deriveCodec[GetRealmDtoIn]

  implicit val GetRealmDtoOutCodec: Codec[GetRealmDtoOut] = deriveCodec[GetRealmDtoOut]

}
