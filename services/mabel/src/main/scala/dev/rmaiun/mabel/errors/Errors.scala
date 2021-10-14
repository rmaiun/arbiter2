package dev.rmaiun.mabel.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object Errors extends ErrorInfo {

  case class UnavailableUsersFound(users: List[String])
      extends AppRuntimeException(
        "unavailableUsersFound",
        s"Unable to process rating for players",
        app,
        Some(Map("users" -> s"${users.mkString(",")}"))
      )
}
