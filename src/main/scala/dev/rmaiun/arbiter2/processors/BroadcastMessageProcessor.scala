package dev.rmaiun.arbiter2.processors

import cats.Monad
import dev.rmaiun.arbiter2.commands.BroadcastMessageCmd
import dev.rmaiun.arbiter2.dtos.{ BotRequest, Definition, ProcessorResponse }
import dev.rmaiun.arbiter2.managers.UserManager
import dev.rmaiun.arbiter2.utils.{ Constants, IdGen }
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.arbiter2.dtos.CmdType.BROADCAST_MSG_CMD
import dev.rmaiun.arbiter2.dtos.{ BotRequest, Definition, ProcessorResponse }
import dev.rmaiun.arbiter2.errors.Errors.NotEnoughRights
import dev.rmaiun.arbiter2.utils.Constants.{ PREFIX, SUFFIX }
import dev.rmaiun.arbiter2.utils.{ Constants, IdGen }
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
