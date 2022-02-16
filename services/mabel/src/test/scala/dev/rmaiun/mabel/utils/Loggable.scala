package dev.rmaiun.mabel.utils

import cats.Monad
import cats.effect.Sync
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait Loggable {
  implicit def unsafeLogger[F[_]: Sync: Monad]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

}
