package dev.rmaiun.datamanager.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object RoutingErrors extends ErrorInfo {
  case class RequiredParamsNotFound(p: Map[String, String])
    extends AppRuntimeException("requiredParamsNotFound", "Required parameters are not found", app, Some(p))
}
