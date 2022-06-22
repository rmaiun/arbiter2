package dev.rmaiun.arbiter2.processors

import dev.rmaiun.arbiter2.dtos.{ BotRequest, ProcessorResponse }
import dev.rmaiun.arbiter2.helpers.{ CmdDefinition, CmdParser }
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.arbiter2.dtos.{ BotRequest, ProcessorResponse }
import dev.rmaiun.arbiter2.helpers.{ CmdDefinition, CmdParser }

trait Processor[F[_]] extends CmdParser[F] with CmdDefinition {
  def process(input: BotRequest): Flow[F, Option[ProcessorResponse]]
}
