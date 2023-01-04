package dev.rmaiun.arbiter2.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object UserErrors {
  case class UserNotFoundException(p: Map[String, String])
      extends AppRuntimeException("userNotFound", s"User is not registered: ${p.mkString(",")}")

  case class NoWritePermissionForUserFoundException(p: Map[String, String])
      extends AppRuntimeException(
        "noWritePermissionForUser",
        s"User has not enough permission to persist data: ${p.mkString(",")}"
      )
  case class UserNotAuthorizedException(p: Map[String, String])
      extends AppRuntimeException("userNotAuthorized", s"User is not authorized: ${p.mkString(",")}")

  case class UserAlreadyExistsException(p: Map[String, String])
      extends AppRuntimeException("userNotFound", s"User already exists: ${p.mkString(",")}")

  case class SameUsersInRoundException(users: List[String])
      extends AppRuntimeException(
        "usersShouldBeDifferent",
        "All players in round must be different",
        Some(Map("users" -> users.mkString(",")))
      )
}
