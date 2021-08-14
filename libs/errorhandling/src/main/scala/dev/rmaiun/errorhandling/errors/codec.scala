package dev.rmaiun.errorhandling.errors

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }
object codec {
  case class ErrorDtoOut(code: String, message: String, app: Option[String], params: Option[Map[String, String]] = None)
  implicit val ErrorDtoOutEncoder: Encoder[ErrorDtoOut] = deriveEncoder[ErrorDtoOut]
  implicit val ErrorDtoOutDecoder: Decoder[ErrorDtoOut] = deriveDecoder[ErrorDtoOut]
}
