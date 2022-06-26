package dev.rmaiun.validation.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

case class ValidationRuntimeException(p: Map[String, String])
    extends AppRuntimeException("dtoValidationFailed", s"Invalid properties were found: $p")
