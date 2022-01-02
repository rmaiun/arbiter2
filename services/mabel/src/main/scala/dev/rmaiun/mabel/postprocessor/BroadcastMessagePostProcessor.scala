package dev.rmaiun.mabel.postprocessor

import cats.Monad
import cats.syntax.foldable._
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.commands.BroadcastMessageCmd
import dev.rmaiun.mabel.dtos.CmdType.BROADCAST_MSG_CMD
import dev.rmaiun.mabel.dtos.{BotRequest, BotResponse, Definition}
import dev.rmaiun.mabel.services.{ArbiterClient, PublisherProxy}
import dev.rmaiun.mabel.utils.Constants._
import dev.rmaiun.mabel.utils.IdGen
import dev.rmaiun.protocol.http.UserDtoSet.UserDto

case class BroadcastMessagePostProcessor[F[_]: Monad](
  arbiterClient: ArbiterClient[F],
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
    arbiterClient.findAllPlayers
      .map(_.items)
      .map(_.filter(_.tid.isDefined))
}
object BroadcastMessagePostProcessor {
  def apply[F[_]](implicit ev: BroadcastMessagePostProcessor[F]): BroadcastMessagePostProcessor[F] = ev
  def impl[F[_]: Monad](ac: ArbiterClient[F], publisherProxy: PublisherProxy[F]): BroadcastMessagePostProcessor[F] =
    new BroadcastMessagePostProcessor[F](ac, publisherProxy)
}
