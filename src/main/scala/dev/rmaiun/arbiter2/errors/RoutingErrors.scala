package dev.rmaiun.arbiter2.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object RoutingErrors {
  case class RequiredParamsNotFound(p: Map[String, String])
      extends AppRuntimeException("requiredParamsNotFound", "Required parameters are not found", Some(p))
}
