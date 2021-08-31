package dev.rmaiun.datamanager.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object RoleErrors extends ErrorInfo {
  case class RoleNotFoundRuntimeException(p: Map[String, String])
    extends AppRuntimeException("roleNotFound", "Role is not found", app, Some(p))
}
