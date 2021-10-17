package dev.rmaiun.mabel.errors

object Errors extends ErrorInfo {

  case class UnavailableUsersFound(users: List[String])
      extends RuntimeException(s"Unable to process rating for players: ${users.mkString(",")}")

  case class NoProcessorFound(cmd: String) extends RuntimeException(s"Invalid command $cmd")

  case class ArbiterClientError(msg: String) extends RuntimeException(msg)
}
