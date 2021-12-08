package dev.rmaiun.mabel.processors

import cats.Monad
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.dtos.CmdType.SEASON_RESULTS_CMD
import dev.rmaiun.mabel.dtos.{ BotRequest, Definition, ProcessorResponse }

case class ForwardProcessor[F[_]: Monad]() extends Processor[F] {
  override def definition: Definition = Definition.query(SEASON_RESULTS_CMD)

  override def process(input: BotRequest): Flow[F, Option[ProcessorResponse]] =
    Flow.pure(None)
}

object ForwardProcessor {
  def apply[F[_]](implicit ev: ForwardProcessor[F]): ForwardProcessor[F] = ev
  def impl[F[_]: Monad]: ForwardProcessor[F] =
    new ForwardProcessor[F]()
}
