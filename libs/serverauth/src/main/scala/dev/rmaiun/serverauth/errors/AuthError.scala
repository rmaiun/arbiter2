package dev.rmaiun.serverauth.errors

case class AuthError(msg: String) extends RuntimeException(msg)
