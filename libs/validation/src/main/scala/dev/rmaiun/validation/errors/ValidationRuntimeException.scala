package dev.rmaiun.validation.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

case class ValidationRuntimeException(p: Map[String, String])
    extends AppRuntimeException("dtoValidationFailed", "Invalid properties were found", None, Some(p))
