package dev.rmaiun.datamanager.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object UserErrors extends ErrorInfo {
  case class UserNotFoundException(p: Map[String, String])
      extends AppRuntimeException("userNotFound", "User is not found", app, Some(p))

  case class RoleNotFoundException(p: Map[String, String])
      extends AppRuntimeException("roleNotFound", "Role is not found", app, Some(p))

}
