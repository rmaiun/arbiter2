package dev.rmaiun.soos.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object SeasonErrors extends ErrorInfo {
  case class SeasonNotFoundRuntimeException(p: Map[String, String])
      extends AppRuntimeException("seasonNotFound", "Season is not found", app, Some(p))
}
