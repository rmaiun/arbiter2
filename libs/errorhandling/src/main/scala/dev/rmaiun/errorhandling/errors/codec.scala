package dev.rmaiun.errorhandling.errors

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
object codec {
  case class ErrorDtoOut(code: String, message: String, params: Option[Map[String, String]] = None)
  implicit val ErrorDtoOutCodec: Codec[ErrorDtoOut] = deriveCodec[ErrorDtoOut]
}
