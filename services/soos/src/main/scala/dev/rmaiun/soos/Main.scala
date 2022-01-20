package dev.rmaiun.soos

import cats.effect.{ ExitCode, IO, IOApp }

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = Server.stream[IO](args).compile.drain.as(ExitCode.Success)
}
