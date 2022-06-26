package dev.rmaiun.arbiter2.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object RoleErrors {
  case class RoleNotFoundRuntimeException(p: Map[String, String])
      extends AppRuntimeException("roleNotFound", "Role is not found", Some(p))
}
