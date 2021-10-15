package dev.rmaiun.mabel.services

import cats.Monad
import cats.effect.Timer
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{ FLog, Flow }
import io.chrisdavenport.log4cats.Logger

import java.util.concurrent.TimeUnit

case class PingManager[F[_]: Timer: Logger: Monad]() {
  def ping(): Flow[F, String] =
    for {
      c <- Flow.pure(Timer[F].clock.realTime(TimeUnit.MILLISECONDS))
      _ <- FLog.info(s"current time $c")
    } yield c.toString
}

object PingManager {
  def apply[F[_]](implicit ev: PingManager[F]): PingManager[F] = ev
  def impl[F[_]: Timer: Logger: Monad]: PingManager[F] =
    new PingManager[F]()
}
