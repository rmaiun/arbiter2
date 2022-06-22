package dev.rmaiun.arbiter2.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object RealmErrors extends ErrorInfo {
  case class RealmNotFoundRuntimeException(p: Map[String, String])
      extends AppRuntimeException("realmNotFound", "Realm is not found", app, Some(p))
}
