package dev.rmaiun.arbiter2.postprocessor

import dev.rmaiun.arbiter2.dtos.BotRequest
import dev.rmaiun.arbiter2.helpers.{ CmdDefinition, CmdParser }
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.arbiter2.helpers.{ CmdDefinition, CmdParser }

trait PostProcessor[F[_]] extends CmdParser[F] with CmdDefinition {
  def postProcess(input: BotRequest): Flow[F, Unit]
}
