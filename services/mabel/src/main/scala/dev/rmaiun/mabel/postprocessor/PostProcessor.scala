package dev.rmaiun.mabel.postprocessor

import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.dtos.BotRequest
import dev.rmaiun.mabel.services.{ CmdDefinition, CmdParser }

trait PostProcessor[F[_]] extends CmdParser[F] with CmdDefinition {
  def postProcess(input: BotRequest): Flow[F, Unit]
}
