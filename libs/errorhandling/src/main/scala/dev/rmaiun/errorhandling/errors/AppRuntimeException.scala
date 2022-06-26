package dev.rmaiun.errorhandling.errors

class AppRuntimeException(
  val code: String,
  val message: String,
  val params: Option[Map[String, String]] = None,
  val cause: Option[Throwable] = None
) extends RuntimeException {

  def this(cause: Throwable) = {
    this("externallyCaused", cause.getMessage, cause = Some(cause))
  }
}
