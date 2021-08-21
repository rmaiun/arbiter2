package dev.rmaiun.datamanager.dtos.api

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

object AlgorithmDtos {
  case class GetAlgorithmDtoIn(algorithm: String)
  case class GetAlgorithmDtoOut(id: Long, algorithm: String)

  case class CreateAlgorithmDtoIn(algorithm: String)
  case class CreateAlgorithmDtoOut(id: Long, algorithm: String)

  case class DeleteAlgorithmDtoIn(id: Long)
  case class DeleteAlgorithmDtoOut(id: Long, quantity: Int)

  object codec {
    implicit val GetAlgorithmDtoInEncoder: Encoder[GetAlgorithmDtoIn] = deriveEncoder[GetAlgorithmDtoIn]
    implicit val GetAlgorithmDtoInDecoder: Decoder[GetAlgorithmDtoIn] = deriveDecoder[GetAlgorithmDtoIn]

    implicit val GetAlgorithmDtoOutEncoder: Encoder[GetAlgorithmDtoOut] = deriveEncoder[GetAlgorithmDtoOut]
    implicit val GetAlgorithmDtoOutDecoder: Decoder[GetAlgorithmDtoOut] = deriveDecoder[GetAlgorithmDtoOut]

    implicit val CreateAlgorithmDtoInEncoder: Encoder[CreateAlgorithmDtoIn] = deriveEncoder[CreateAlgorithmDtoIn]
    implicit val CreateAlgorithmDtoInDecoder: Decoder[CreateAlgorithmDtoIn] = deriveDecoder[CreateAlgorithmDtoIn]

    implicit val CreateAlgorithmDtoOutEncoder: Encoder[CreateAlgorithmDtoOut] = deriveEncoder[CreateAlgorithmDtoOut]
    implicit val CreateAlgorithmDtoOutDecoder: Decoder[CreateAlgorithmDtoOut] = deriveDecoder[CreateAlgorithmDtoOut]

    implicit val DeleteAlgorithmDtoInEncoder: Encoder[DeleteAlgorithmDtoIn] = deriveEncoder[DeleteAlgorithmDtoIn]
    implicit val DeleteAlgorithmDtoInDecoder: Decoder[DeleteAlgorithmDtoIn] = deriveDecoder[DeleteAlgorithmDtoIn]

    implicit val DeleteAlgorithmDtoOutEncoder: Encoder[DeleteAlgorithmDtoOut] = deriveEncoder[DeleteAlgorithmDtoOut]
    implicit val DeleteAlgorithmDtoOutDecoder: Decoder[DeleteAlgorithmDtoOut] = deriveDecoder[DeleteAlgorithmDtoOut]
  }
}
