package dev.rmaiun.mabel.postprocessor

import cats.syntax.foldable._
import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.mabel.commands.AddPlayerCmd
import dev.rmaiun.mabel.dtos.{ BotRequest, BotResponse }
import dev.rmaiun.mabel.services.{ ArbiterClient, PublisherProxy }
import dev.rmaiun.mabel.utils.Constants.{ PREFIX, SUFFIX }
import dev.rmaiun.mabel.utils.IdGen
import dev.rmaiun.protocol.http.UserDtoSet.UserRoleData
import io.chrisdavenport.log4cats.Logger

case class AddPlayerPostProcessor[F[_]: MonadThrowable: Logger](
  arbiterClient: ArbiterClient[F],
  cmdPublisher: PublisherProxy[F]
) extends PostProcessor[F] {
  override def postProcess(input: BotRequest): Flow[F, Unit] =
    for {
      cmd <- parseDto[AddPlayerCmd](input.data)
      _   <- cmdPublisher.publishToBot(BotResponse(cmd.tid, IdGen.msgId, createOutput))
      _   <- notifyAdmins(cmd)
    } yield ()

  private def notifyAdmins(cmd: AddPlayerCmd): Flow[F, Unit] =
    for {
      adminsDto <- arbiterClient.findRealmAdmins
      admins     = adminsDto.adminUsers.filter(urd => urd.tid.fold(false)(tid => tid != cmd.moderator))
      _         <- sendToAdministrators(admins, cmd.surname)
    } yield ()

  private def sendToAdministrators(admins: List[UserRoleData], player: String): Flow[F, Unit] = {
    val msg = s"New player ${player.capitalize} is registered."
    admins
      .map(d => cmdPublisher.publishToBot(BotResponse(d.tid.getOrElse(0), IdGen.msgId, PREFIX + msg + SUFFIX)))
      .sequence_
  }

  private def createOutput: String =
    s"""$PREFIX Congrats!
       |Your were successfully activated $SUFFIX""".stripMargin
}

object AddPlayerPostProcessor {
  def apply[F[_]](implicit ev: AddPlayerPostProcessor[F]): AddPlayerPostProcessor[F] = ev
  def impl[F[_]: MonadThrowable: Logger](
    ac: ArbiterClient[F],
    cmdPublisher: PublisherProxy[F]
  ): AddPlayerPostProcessor[F] =
    new AddPlayerPostProcessor[F](ac, cmdPublisher)
}
