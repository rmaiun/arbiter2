package dev.rmaiun.arbiter2.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object SeasonErrors {
  case class SeasonNotFoundRuntimeException(p: Map[String, String])
      extends AppRuntimeException("seasonNotFound", "Season is not found", Some(p))
}
