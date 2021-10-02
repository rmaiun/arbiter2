package dev.rmaiun.datamanager.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object UserErrors extends ErrorInfo {
  case class UserNotFoundException(p: Map[String, String])
      extends AppRuntimeException("userNotFound", "User is not registered", app, Some(p))

  case class NoWritePermissionForUserFoundException(p: Map[String, String])
      extends AppRuntimeException(
        "noWritePermissionForUser",
        "User has not enough permission to persist data",
        app,
        Some(p)
      )
  case class UserNotAuthorizedException(p: Map[String, String])
      extends AppRuntimeException("userNotAuthorized", "User is not authorized", app, Some(p))

  case class UserAlreadyExistsException(p: Map[String, String])
      extends AppRuntimeException("userNotFound", "User already exists", app, Some(p))

  case class SameUsersInRoundException(users: List[String])
      extends AppRuntimeException(
        "usersShouldBeDifferent",
        "All players in round must be different",
        app,
        Some(Map("users" -> users.mkString(",")))
      )
}
