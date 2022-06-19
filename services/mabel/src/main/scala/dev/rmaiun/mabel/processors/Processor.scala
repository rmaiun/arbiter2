package dev.rmaiun.mabel.processors

import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.dtos.{ BotRequest, ProcessorResponse }
import dev.rmaiun.mabel.helpers.{ CmdDefinition, CmdParser }

trait Processor[F[_]] extends CmdParser[F] with CmdDefinition {
  def process(input: BotRequest): Flow[F, Option[ProcessorResponse]]
}
