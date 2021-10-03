package dev.rmaiun.datamanager.dtos.api.codec

import dev.rmaiun.datamanager.dtos.api.RealmDtoSet._
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

trait RealmDtoCodec extends SubDtoCodec {
  implicit val RegisterRealmDtoInEncoder: Encoder[RegisterRealmDtoIn] = deriveEncoder[RegisterRealmDtoIn]
  implicit val RegisterRealmDtoInDecoder: Decoder[RegisterRealmDtoIn] = deriveDecoder[RegisterRealmDtoIn]

  implicit val RegisterRealmDtoOutEncoder: Encoder[RegisterRealmDtoOut] = deriveEncoder[RegisterRealmDtoOut]
  implicit val RegisterRealmDtoOutDecoder: Decoder[RegisterRealmDtoOut] = deriveDecoder[RegisterRealmDtoOut]

  implicit val UpdateRealmAlgorithmDtoInEncoder: Encoder[UpdateRealmAlgorithmDtoIn] =
    deriveEncoder[UpdateRealmAlgorithmDtoIn]
  implicit val UpdateRealmAlgorithmDtoInDecoder: Decoder[UpdateRealmAlgorithmDtoIn] =
    deriveDecoder[UpdateRealmAlgorithmDtoIn]

  implicit val UpdateRealmAlgorithmDtoOutEncoder: Encoder[UpdateRealmAlgorithmDtoOut] =
    deriveEncoder[UpdateRealmAlgorithmDtoOut]
  implicit val UpdateRealmAlgorithmDtoOutDecoder: Decoder[UpdateRealmAlgorithmDtoOut] =
    deriveDecoder[UpdateRealmAlgorithmDtoOut]

  implicit val GetRealmDtoInEncoder: Encoder[GetRealmDtoIn] = deriveEncoder[GetRealmDtoIn]
  implicit val GetRealmDtoInDecoder: Decoder[GetRealmDtoIn] = deriveDecoder[GetRealmDtoIn]

  implicit val GetRealmDtoOutEncoder: Encoder[GetRealmDtoOut] = deriveEncoder[GetRealmDtoOut]
  implicit val GetRealmDtoOutDecoder: Decoder[GetRealmDtoOut] = deriveDecoder[GetRealmDtoOut]

}
