package dev.rmaiun.arbiter2.errors

object Errors {

  case class UnavailableUsersFound(users: List[String])
      extends RuntimeException(s"Unable to process rating for players: ${users.mkString(",")}")

  case class NoProcessorFound(cmd: String) extends RuntimeException(s"Invalid command $cmd")

  case class UserIsNotAuthorized(cause: Throwable) extends RuntimeException("User is not authorized", cause)
  case class NotEnoughRights(msg: String)          extends RuntimeException(msg)
}
