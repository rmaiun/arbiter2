package dev.rmaiun.arbiter2.postprocessor

import cats.Monad
import cats.syntax.foldable._
import cats.syntax.option._
import dev.rmaiun.arbiter2.commands.BroadcastMessageCmd
import dev.rmaiun.arbiter2.dtos.{ BotRequest, BotResponse, Definition }
import dev.rmaiun.arbiter2.helpers.PublisherProxy
import dev.rmaiun.arbiter2.managers.UserManager
import dev.rmaiun.arbiter2.utils.{ Constants, IdGen }
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.arbiter2.dtos.CmdType.BROADCAST_MSG_CMD
import dev.rmaiun.arbiter2.dtos.{ BotRequest, BotResponse, Definition }
import Constants._
import dev.rmaiun.arbiter2.utils.{ Constants, IdGen }
import dev.rmaiun.protocol.http.UserDtoSet.{ FindAllUsersDtoIn, UserDto }

case class BroadcastMessagePostProcessor[F[_]: Monad](
  userManager: UserManager[F],
  publisherProxy: PublisherProxy[F]
) extends PostProcessor[F] {
  override def definition: Definition = Definition.internal(BROADCAST_MSG_CMD)

  override def postProcess(input: BotRequest): Flow[F, Unit] =
    for {
      cmd <- parseDto[BroadcastMessageCmd](input.data)
      _   <- processBroadcast(cmd)
    } yield ()

  private def processBroadcast(cmd: BroadcastMessageCmd): Flow[F, Unit] =
    if (cmd.testMode) {
      replyToSenderOnly(cmd)
    } else {
      broadcastToRealmPlayers(cmd)
    }

  private def replyToSenderOnly(cmd: BroadcastMessageCmd): Flow[F, Unit] =
    publisherProxy.publishToBot(BotResponse(cmd.moderatorTid, IdGen.msgId, cmd.text.toBotMsg))

  private def broadcastToRealmPlayers(cmd: BroadcastMessageCmd): Flow[F, Unit] =
    for {
      players <- findRelevantPlayers
      _       <- notifyAll(players, cmd.text)
    } yield ()

  private def notifyAll(players: List[UserDto], msg: String): Flow[F, Unit] =
    players
      .map(p => BotResponse(p.tid.getOrElse(-1), IdGen.msgId, msg.toBotMsg))
      .map(publisherProxy.publishToBot)
      .sequence_

  private def findRelevantPlayers: Flow[F, List[UserDto]] =
    userManager
      .findAllUsers(FindAllUsersDtoIn(Constants.defaultRealm, true.some))
      .map(_.items)
      .map(_.filter(_.tid.isDefined))
}
object BroadcastMessagePostProcessor {
  def apply[F[_]](implicit ev: BroadcastMessagePostProcessor[F]): BroadcastMessagePostProcessor[F] = ev
  def impl[F[_]: Monad](
    userManager: UserManager[F],
    publisherProxy: PublisherProxy[F]
  ): BroadcastMessagePostProcessor[F] =
    new BroadcastMessagePostProcessor[F](userManager, publisherProxy)
}
