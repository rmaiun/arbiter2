package dev.rmaiun.mabel.processors

import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.dtos.{BotRequest, ProcessorResponse}
import dev.rmaiun.mabel.services.{CmdDefinition, CmdParser}

trait Processor[F[_]] extends CmdParser[F] with CmdDefinition {
  def process(input: BotRequest): Flow[F, Option[ProcessorResponse]]
}
