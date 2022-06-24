package dev.rmaiun.arbiter2.helpers

import cats.Monad
import cats.effect.Sync
import cats.syntax.apply._
import cats.syntax.foldable._
import cats.syntax.option._
import dev.profunktor.fs2rabbit.model.AmqpEnvelope
import dev.rmaiun.arbiter2.dtos.{ BotRequest, CmdType, ProcessorResponse }
import dev.rmaiun.arbiter2.errors.Errors.{ NoProcessorFound, UserIsNotAuthorized }
import dev.rmaiun.arbiter2.managers.UserManager
import dev.rmaiun.arbiter2.postprocessor.PostProcessor
import dev.rmaiun.arbiter2.processors.Processor
import dev.rmaiun.arbiter2.utils.Constants.{ PREFIX, SUFFIX }
import dev.rmaiun.arbiter2.utils.IdGen
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{ FLog, Flow }
import dev.rmaiun.protocol.http.UserDtoSet.FindUserDtoIn
import io.circe.parser._
import org.http4s.client.ConnectionFailure
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.lang.System.currentTimeMillis

case class CommandHandler[F[_]: Sync](commandType: CmdType)(
  userManager: UserManager[F],
  processors: List[Processor[F]],
  postProcessors: List[PostProcessor[F]],
  publisherProxy: PublisherProxy[F]
) {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)
  import dev.rmaiun.arbiter2.dtos.BotRequest._
  def process(record: AmqpEnvelope[String]): Flow[F, Unit] = {
    val start = currentTimeMillis
    for {
      json    <- Flow.fromEither(parse(new String(record.payload)))
      input   <- Flow.fromEither(BotRequestCodec.decodeJson(json))
      surname <- checkUserRegistered(input)
      _       <- process(input)
      _       <- FLog.info(s"$commandType| Cmd ${input.cmd} ($surname) was processed in ${currentTimeMillis() - start} ms")
    } yield ()
  }

  private def checkUserRegistered(input: BotRequest): Flow[F, String] =
    userManager
      .findUser(FindUserDtoIn(tid = input.tid.some))
      .map(_.user.surname.capitalize)
      .leftFlatMap(err =>
        FLog.info(s"$commandType| User ${input.user} (${input.tid}) tried to process ${input.cmd}") *>
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
        ppList <- selectPostProcessor(input.cmd)
        _      <- ppList.map(_.postProcess(input)).sequence_
      } yield ()
    }
  }

  private def processInput(input: BotRequest): Flow[F, Option[ProcessorResponse]] = {
    val flow = for {
      processor <- selectProcessor(input.cmd)
      result    <- processor.process(input)
    } yield result
    flow.leftFlatMap { err =>
      val msg = err match {
        case _: ConnectionFailure => s"$PREFIX ERROR: Connection issue $SUFFIX"
        case _                    => s"$PREFIX ERROR: ${err.getMessage} $SUFFIX"
      }
      FLog.error(s"$commandType| ${err.getMessage}") *>
        Flow.effect(Monad[F].pure(err.printStackTrace())) *>
        Flow.pure(Some(ProcessorResponse.error(input.chatId, IdGen.msgId, msg)))
    }

  }

  private def sendResponse(pr: Option[ProcessorResponse]): Flow[F, Unit] =
    pr match {
      case Some(value) => publisherProxy.publishToBot(value.botResponse)
      case None        => Flow.unit
    }

  private def selectProcessor(cmd: String): Flow[F, Processor[F]] =
    Flow.fromOpt(
      processors.find(p => p.definition.cmdType == commandType && p.definition.supportCommands.contains(cmd)),
      NoProcessorFound(cmd)
    )

  private def selectPostProcessor(cmd: String): Flow[F, List[PostProcessor[F]]] =
    Flow.pure(postProcessors.filter(_.definition.supportCommands.contains(cmd)))

}

object CommandHandler {
  def apply[F[_]](implicit ev: CommandHandler[F]): CommandHandler[F] = ev
  def impl[F[_]: Sync](cmdType: CmdType)(
    userManager: UserManager[F],
    processors: List[Processor[F]],
    postProcessors: List[PostProcessor[F]],
    publisherProxy: PublisherProxy[F]
  ): CommandHandler[F] =
    new CommandHandler[F](cmdType)(userManager, processors, postProcessors, publisherProxy)
}
