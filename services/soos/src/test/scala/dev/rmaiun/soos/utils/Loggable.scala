package dev.rmaiun.soos.utils

import cats.Monad
import cats.effect.Sync
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

trait Loggable {
  implicit def unsafeLogger[F[_]: Sync: Monad]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

}
