package dev.rmaiun.mabel.services

import cats.Monad
import cats.syntax.apply._
import cats.syntax.foldable._
import dev.profunktor.fs2rabbit.model.AmqpEnvelope
import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.flowtypes.{ FLog, Flow }
import dev.rmaiun.mabel.dtos.{ BotRequest, ProcessorResponse }
import dev.rmaiun.mabel.errors.Errors.UserIsNotAuthorized
import dev.rmaiun.mabel.utils.Constants.{ PREFIX, SUFFIX }
import dev.rmaiun.mabel.utils.IdGen
import io.chrisdavenport.log4cats.Logger
import io.circe.parser._
import org.http4s.client.ConnectionFailure

import java.lang.System.currentTimeMillis

case class CommandHandler[F[_]: MonadThrowable: Logger](
  arbiterClient: ArbiterClient[F],
  strategy: ProcessorStrategy[F],
  publisherProxy: PublisherProxy[F]
) {
  import dev.rmaiun.mabel.dtos.BotRequest._
  def process(record: AmqpEnvelope[String]): Flow[F, Unit] = {
    val start = currentTimeMillis
    for {
      json    <- Flow.fromEither(parse(new String(record.payload)))
      input   <- Flow.fromEither(BotRequestDecoder.decodeJson(json))
      surname <- checkUserRegistered(input)
      _       <- process(input)
      _       <- FLog.info(s"Cmd ${input.cmd} ($surname) was processed in ${currentTimeMillis() - start} ms")
    } yield ()
  }

  private def checkUserRegistered(input: BotRequest): Flow[F, String] =
    arbiterClient
      .findPlayerByTid(input.tid)
      .map(_.user.surname.capitalize)
      .leftFlatMap(err =>
        FLog.info(s"User ${input.user} (${input.tid}) tried to process ${input.cmd}") *>
          sendResponse(
            Some(ProcessorResponse.error(input.chatId, IdGen.msgId, s"$PREFIX You are not authorized $SUFFIX"))
          ) *>
          Flow.error(UserIsNotAuthorized(err))
      )

  private def process(input: BotRequest): Flow[F, Unit] = {
    val processResult = for {
      result <- processInput(input)
      _      <- sendResponse(result)
    } yield result

    processResult.flatMap(r => postProcess(input, r))
  }

  private def postProcess(input: BotRequest, processorResponse: Option[ProcessorResponse]): Flow[F, Unit] = {
    val isErrorResponse = processorResponse.fold(false)(pr => pr.error)
    if (isErrorResponse) {
      Flow.unit
    } else {
      for {
        ppList <- strategy.selectPostProcessor(input.cmd)
        _      <- ppList.map(_.postProcess(input)).sequence_
      } yield ()
    }
  }

  private def processInput(input: BotRequest): Flow[F, Option[ProcessorResponse]] = {
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
        Flow.pure(Some(ProcessorResponse.error(input.chatId, IdGen.msgId, msg)))
    }

  }

  private def sendResponse(pr: Option[ProcessorResponse]): Flow[F, Unit] =
    pr match {
      case Some(value) => publisherProxy.publishToBot(value.botResponse)
      case None        => Flow.unit
    }
}

object CommandHandler {
  def apply[F[_]](implicit ev: CommandHandler[F]): CommandHandler[F] = ev
  def impl[F[_]: MonadThrowable: Logger](
    arbiterClient: ArbiterClient[F],
    ps: ProcessorStrategy[F],
    publisherProxy: PublisherProxy[F]
  ): CommandHandler[F] =
    new CommandHandler[F](arbiterClient, ps, publisherProxy)
}
