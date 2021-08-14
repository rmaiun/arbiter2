package dev.rmaiun.datamanager.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object AlgorithmErrors extends ErrorInfo {
  case class AlgorithmNotFoundRuntimeException(p: Map[String, String])
      extends AppRuntimeException("algorithmNotFound", "Algorithm is not found", app, Some(p))

}
