package dev.rmaiun.arbiter2.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object AlgorithmErrors {
  case class AlgorithmNotFoundException(p: Map[String, String])
      extends AppRuntimeException("algorithmNotFound", s"Algorithm is not found", Some(p))

}
