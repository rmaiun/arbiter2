package dev.rmaiun.mabel.processors

import cats.Monad
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.commands.BroadcastMessageCmd
import dev.rmaiun.mabel.dtos.CmdType.BROADCAST_MSG_CMD
import dev.rmaiun.mabel.dtos.{ BotRequest, Definition, ProcessorResponse }
import dev.rmaiun.mabel.errors.Errors.NotEnoughRights
import dev.rmaiun.mabel.managers.UserManager
import dev.rmaiun.mabel.utils.Constants.{ PREFIX, SUFFIX }
import dev.rmaiun.mabel.utils.{ Constants, IdGen }
import dev.rmaiun.protocol.http.UserDtoSet.FindRealmAdminsDtoIn

case class BroadcastMessageProcessor[F[_]: Monad](
  userManager: UserManager[F]
) extends Processor[F] {
  override def definition: Definition = Definition.persistence(BROADCAST_MSG_CMD)

  override def process(input: BotRequest): Flow[F, Option[ProcessorResponse]] =
    for {
      dto <- parseDto[BroadcastMessageCmd](input.data)
      _   <- checkUserIsAdmin(dto.moderatorTid)
    } yield {
      val msg = s"$PREFIX Your message will be broadcast [testMode: ${dto.testMode}] $SUFFIX"
      Some(ProcessorResponse.ok(input.chatId, IdGen.msgId, msg))
    }

  private def checkUserIsAdmin(tid: Long): Flow[F, Unit] =
    userManager
      .findRealmAdmins(FindRealmAdminsDtoIn(Constants.defaultRealm))
      .map(dto => dto.adminUsers.find(_.tid.contains(tid)))
      .flatMap {
        case Some(_) => Flow.unit
        case None    => Flow.error(NotEnoughRights("You don't have enough rights to broadcast message"))
      }
}

object BroadcastMessageProcessor {
  def apply[F[_]](implicit ev: BroadcastMessageProcessor[F]): BroadcastMessageProcessor[F] = ev
  def impl[F[_]: Monad](userManager: UserManager[F]): BroadcastMessageProcessor[F] =
    new BroadcastMessageProcessor[F](userManager)
}
