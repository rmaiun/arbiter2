package dev.rmaiun.mabel.services

import cats.Monad
import dev.profunktor.fs2rabbit.model.{AmqpEnvelope, AmqpMessage, AmqpProperties}
import dev.rmaiun.flowtypes.Flow.{Flow, MonadThrowable, error}
import dev.rmaiun.flowtypes.{FLog, Flow}
import dev.rmaiun.mabel.dtos.AmqpStructures.AmqpPublisher
import dev.rmaiun.mabel.dtos.BotResponse._
import dev.rmaiun.mabel.dtos.{BotRequest, ProcessorResponse}
import dev.rmaiun.mabel.utils.Constants.{PREFIX, SUFFIX}
import dev.rmaiun.mabel.utils.IdGenerator
import io.chrisdavenport.log4cats.Logger
import io.circe.parser._
import cats.syntax.apply._

case class CommandHandler[F[_]: MonadThrowable: Logger](
  strategy: ProcessorStrategy[F],
  publisher: AmqpPublisher[F]
) {
  import dev.rmaiun.mabel.dtos.BotRequest._
  def process(record: AmqpEnvelope[String]): Flow[F, Unit] = {
    val start = System.currentTimeMillis
    for {
      json   <- Flow.fromEither(parse(new String(record.payload)))
      input  <- Flow.fromEither(BotRequestDecoder.decodeJson(json))
      result <- processInput(input)
      _      <- sendResponse(result)
      _      <- FLog.info(s"Cmd ${input.cmd} (${input.user}) was processed in ${System.currentTimeMillis() - start} ms")
    } yield result

  }

  private def processInput(input: BotRequest): Flow[F, ProcessorResponse] = {
    val flow = for {
      processor <- strategy.selectProcessor(input.cmd)
      result    <- processor.process(input)
    } yield result
    flow.leftFlatMap(err =>
      FLog.error(err.getMessage) *>
      Flow.effect(Monad[F].pure(err.printStackTrace())) *>
      Flow.pure(ProcessorResponse.error(input.chatId, IdGenerator.msgId, s"$PREFIX ERROR: ${err.getMessage} $SUFFIX"))
    )
  }

  private def sendResponse(pr: ProcessorResponse): Flow[F, Unit] = {
    val payload = BotResponseEncoder(pr.botResponse).toString()
    val msg     = AmqpMessage[String](payload, AmqpProperties())
    Flow.effect(publisher(msg))
  }
}

object CommandHandler {
  def apply[F[_]](implicit ev: CommandHandler[F]): CommandHandler[F] = ev
  def impl[F[_]: MonadThrowable: Logger](ps: ProcessorStrategy[F], publisher: AmqpPublisher[F]): CommandHandler[F] =
    new CommandHandler[F](ps, publisher)
}
