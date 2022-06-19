package dev.rmaiun.mabel.postprocessor

import cats.effect.Sync
import cats.syntax.foldable._
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.commands.AddPlayerCmd
import dev.rmaiun.mabel.dtos.CmdType.ADD_PLAYER_CMD
import dev.rmaiun.mabel.dtos.{ BotRequest, BotResponse, Definition }
import dev.rmaiun.mabel.helpers.PublisherProxy
import dev.rmaiun.mabel.services.ArbiterClient
import dev.rmaiun.mabel.utils.Constants._
import dev.rmaiun.mabel.utils.IdGen
import dev.rmaiun.protocol.http.UserDtoSet.UserRoleData
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

case class AddPlayerPostProcessor[F[_]: Sync](
  arbiterClient: ArbiterClient[F],
  cmdPublisher: PublisherProxy[F]
) extends PostProcessor[F] {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)

  override def definition: Definition = Definition.internal(ADD_PLAYER_CMD)

  override def postProcess(input: BotRequest): Flow[F, Unit] =
    for {
      cmd <- parseDto[AddPlayerCmd](input.data)
      _   <- cmdPublisher.publishToBot(BotResponse(cmd.tid.getOrElse(-1), IdGen.msgId, createOutput))
      _   <- notifyAdmins(cmd)
    } yield ()

  private def notifyAdmins(cmd: AddPlayerCmd): Flow[F, Unit] =
    for {
      adminsDto <- arbiterClient.findRealmAdmins()
      admins     = adminsDto.adminUsers.filter(urd => urd.tid.fold(false)(tid => tid != cmd.moderator))
      _         <- sendToAdministrators(admins, cmd.surname)
    } yield ()

  private def sendToAdministrators(admins: List[UserRoleData], player: String): Flow[F, Unit] = {
    val msg = s"New player ${player.capitalize} is registered."
    admins
      .map(d => cmdPublisher.publishToBot(BotResponse(d.tid.getOrElse(0), IdGen.msgId, msg.toBotMsg)))
      .sequence_
  }

  private def createOutput: String =
    s"""Congrats!
       |Your were successfully activated""".stripMargin.toBotMsg
}

object AddPlayerPostProcessor {
  def apply[F[_]](implicit ev: AddPlayerPostProcessor[F]): AddPlayerPostProcessor[F] = ev
  def impl[F[_]: Sync](
    ac: ArbiterClient[F],
    cmdPublisher: PublisherProxy[F]
  ): AddPlayerPostProcessor[F] =
    new AddPlayerPostProcessor[F](ac, cmdPublisher)
}
