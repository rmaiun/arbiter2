package dev.rmaiun.mabel.services

import cats.Monad
import cats.syntax.apply._
import cats.syntax.foldable._
import dev.profunktor.fs2rabbit.model.AmqpEnvelope
import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.flowtypes.{ FLog, Flow }
import dev.rmaiun.mabel.dtos.{ BotRequest, ProcessorResponse }
import dev.rmaiun.mabel.utils.Constants.{ PREFIX, SUFFIX }
import dev.rmaiun.mabel.utils.IdGen
import io.chrisdavenport.log4cats.Logger
import io.circe.parser._
import org.http4s.client.ConnectionFailure

case class CommandHandler[F[_]: MonadThrowable: Logger](
  strategy: ProcessorStrategy[F],
  publisherProxy: PublisherProxy[F]
) {
  import dev.rmaiun.mabel.dtos.BotRequest._
  def process(record: AmqpEnvelope[String]): Flow[F, Unit] = {
    val start = System.currentTimeMillis
    for {
      json  <- Flow.fromEither(parse(new String(record.payload)))
      input <- Flow.fromEither(BotRequestDecoder.decodeJson(json))
      _     <- process(input)
      _     <- FLog.info(s"Cmd ${input.cmd} (${input.user}) was processed in ${System.currentTimeMillis() - start} ms")
    } yield ()
  }

  private def process(input: BotRequest): Flow[F, Unit] = {
    val processResult = for {
      result <- processInput(input)
      _      <- sendResponse(result)
    } yield result

    processResult.flatMap(r => postProcess(input, r))
  }

  private def postProcess(input: BotRequest, processorResponse: ProcessorResponse): Flow[F, Unit] =
    if (processorResponse.error) {
      Flow.unit
    } else {
      for {
        ppList <- strategy.selectPostProcessor(input.cmd)
        _      <- ppList.map(_.postProcess(input)).sequence_
      } yield ()
    }

  private def processInput(input: BotRequest): Flow[F, ProcessorResponse] = {
    val flow = for {
      processor <- strategy.selectProcessor(input.cmd)
      result    <- processor.process(input)
    } yield result
    flow.leftFlatMap { err =>
      val msg = err match {
        case _: ConnectionFailure => s"$PREFIX ERROR: Connection issue $SUFFIX"
        case _                    => s"$PREFIX ERROR: ${err.getMessage} $SUFFIX"
      }
      FLog.error(err.getMessage) *>
        Flow.effect(Monad[F].pure(err.printStackTrace())) *>
        Flow.pure(ProcessorResponse.error(input.chatId, IdGen.msgId, msg))
    }

  }

  private def sendResponse(pr: ProcessorResponse): Flow[F, Unit] =
    publisherProxy.publishToBot(pr.botResponse)
}

object CommandHandler {
  def apply[F[_]](implicit ev: CommandHandler[F]): CommandHandler[F] = ev
  def impl[F[_]: MonadThrowable: Logger](
    ps: ProcessorStrategy[F],
    publisherProxy: PublisherProxy[F]
  ): CommandHandler[F] =
    new CommandHandler[F](ps, publisherProxy)
}
