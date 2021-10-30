package dev.rmaiun.mabel.processors

import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.dtos.{BotRequest, ProcessorResponse}
import dev.rmaiun.mabel.services.CmdParser

trait Processor[F[_]] extends CmdParser[F]{
  def process(input: BotRequest): Flow[F, ProcessorResponse]
}
