package dev.rmaiun.mabel

import cats.effect.{ ExitCode, IO, IOApp }

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = Server.stream[IO].compile.drain.as(ExitCode.Success)
}
