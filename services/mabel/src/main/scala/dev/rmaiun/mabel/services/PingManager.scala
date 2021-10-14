package dev.rmaiun.mabel.services

import cats.Monad
import cats.effect.Clock
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{ FLog, Flow }
import io.chrisdavenport.log4cats.Logger

import java.util.concurrent.TimeUnit

case class PingManager[F[_]: Clock: Logger: Monad]() {
  def ping(): Flow[F, String] =
    for {
      c <- Flow.pure(Clock[F].realTime(TimeUnit.MILLISECONDS))
      _ <- FLog.info(s"current time $c")
    } yield c.toString
}
