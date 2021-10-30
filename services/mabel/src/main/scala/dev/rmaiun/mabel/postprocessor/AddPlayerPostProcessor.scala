package dev.rmaiun.mabel.postprocessor

import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.mabel.commands.AddPlayerCmd
import dev.rmaiun.mabel.dtos.{ BotRequest, BotResponse }
import dev.rmaiun.mabel.services.{ ArbiterClient, CmdPublisher }
import dev.rmaiun.mabel.utils.Constants.{ PREFIX, SUFFIX }
import dev.rmaiun.mabel.utils.IdGen
import io.chrisdavenport.log4cats.Logger

case class AddPlayerPostProcessor[F[_]: MonadThrowable: Logger](
  arbiterClient: ArbiterClient[F],
  cmdPublisher: CmdPublisher[F]
) extends PostProcessor[F] {
  override def postProcess(input: BotRequest): Flow[F, Unit] =
    for {
      cmd <- parseDto[AddPlayerCmd](input.data)
      _   <- cmdPublisher.publishToBot(BotResponse(cmd.tid, IdGen.msgId, createOutput))
    } yield ()

  private def createOutput: String =
    s"""$PREFIX Congrats!
       |Your were successfully activated $SUFFIX""".stripMargin
}

object AddPlayerPostProcessor {
  def apply[F[_]](implicit ev: AddPlayerPostProcessor[F]): AddPlayerPostProcessor[F] = ev
  def impl[F[_]: MonadThrowable: Logger](
    ac: ArbiterClient[F],
    cmdPublisher: CmdPublisher[F]
  ): AddPlayerPostProcessor[F] =
    new AddPlayerPostProcessor[F](ac, cmdPublisher)
}
