package dev.rmaiun.mabel.postprocessor

import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.dtos.BotRequest
import dev.rmaiun.mabel.services.CmdParser

trait PostProcessor[F[_]] extends CmdParser[F] {
  def postProcess(input: BotRequest): Flow[F, Unit]
}
