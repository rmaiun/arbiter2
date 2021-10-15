package dev.rmaiun.mabel.services

import cats.Applicative
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.processors.{ AddPlayerProcessor, AddRoundProcessor, Processor }

case class ProcessorStrategy[F[_]: Applicative](
  addPlayerProcessor: AddPlayerProcessor[F],
  addRoundProcessor: AddRoundProcessor[F]
) {
  def selectProcessor(cmd: String): Flow[F, Processor[F]] =
    cmd match {
      case "addPlayer" => Flow.pure(addPlayerProcessor)
      case "addRound"  => Flow.pure(addRoundProcessor)
      case _           => Flow.error(new RuntimeException("Unable to process given request"))
    }
}

object ProcessorStrategy {
  def apply[F[_]](implicit ev: ProcessorStrategy[F]): ProcessorStrategy[F] = ev
  def impl[F[_]: Applicative](
    addPlayerProcessor: AddPlayerProcessor[F],
    addRoundProcessor: AddRoundProcessor[F]
  ): ProcessorStrategy[F] =
    new ProcessorStrategy[F](addPlayerProcessor, addRoundProcessor)
}
