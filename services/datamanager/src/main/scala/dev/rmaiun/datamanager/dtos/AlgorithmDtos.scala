package dev.rmaiun.datamanager.dtos

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

object AlgorithmDtos {
  case class GetAlgorithmDtoIn(algorithm: String)
  case class GetAlgorithmDtoOut(id: Long, algorithm: String)
  case class CreateAlgorithmDtoIn(algorithm: String)
  case class CreateAlgorithmDtoOut(id: Long, algorithm: String)

  object codec {
    implicit val GetAlgorithmDtoInEncoder: Encoder[GetAlgorithmDtoIn] = deriveEncoder[GetAlgorithmDtoIn]
    implicit val GetAlgorithmDtoInDecoder: Decoder[GetAlgorithmDtoIn] = deriveDecoder[GetAlgorithmDtoIn]

    implicit val GetAlgorithmDtoOutEncoder: Encoder[GetAlgorithmDtoOut] = deriveEncoder[GetAlgorithmDtoOut]
    implicit val GetAlgorithmDtoOutDecoder: Decoder[GetAlgorithmDtoOut] = deriveDecoder[GetAlgorithmDtoOut]

    implicit val CreateAlgorithmDtoInEncoder: Encoder[CreateAlgorithmDtoIn] = deriveEncoder[CreateAlgorithmDtoIn]
    implicit val CreateAlgorithmDtoInDecoder: Decoder[CreateAlgorithmDtoIn] = deriveDecoder[CreateAlgorithmDtoIn]

    implicit val CreateAlgorithmDtoOutEncoder: Encoder[CreateAlgorithmDtoOut] = deriveEncoder[CreateAlgorithmDtoOut]
    implicit val CreateAlgorithmDtoOutDecoder: Decoder[CreateAlgorithmDtoOut] = deriveDecoder[CreateAlgorithmDtoOut]
  }
}
