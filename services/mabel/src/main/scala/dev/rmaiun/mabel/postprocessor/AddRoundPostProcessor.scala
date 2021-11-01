package dev.rmaiun.mabel.postprocessor

import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.mabel.commands.AddRoundCmd
import dev.rmaiun.mabel.dtos.{ BotRequest, BotResponse }
import dev.rmaiun.mabel.services.{ ArbiterClient, PublisherProxy }
import dev.rmaiun.mabel.utils.Constants.{ PREFIX, SUFFIX }
import dev.rmaiun.mabel.utils.IdGen
import io.chrisdavenport.log4cats.Logger

case class AddRoundPostProcessor[F[_]: MonadThrowable: Logger](
  arbiterClient: ArbiterClient[F],
  cmdPublisher: PublisherProxy[F]
) extends PostProcessor[F] {
  override def postProcess(input: BotRequest): Flow[F, Unit] =
    for {
      cmd <- parseDto[AddRoundCmd](input.data)
      _   <- sendNotificationToUser(cmd.w1, s"${cmd.l1.capitalize}/${cmd.l2.capitalize}", win = true)
      _   <- sendNotificationToUser(cmd.w2, s"${cmd.l1.capitalize}/${cmd.l2.capitalize}", win = true)
      _   <- sendNotificationToUser(cmd.l1, s"${cmd.w1.capitalize}/${cmd.w2.capitalize}", win = false)
      _   <- sendNotificationToUser(cmd.l2, s"${cmd.w1.capitalize}/${cmd.w2.capitalize}", win = false)
    } yield ()

  private def sendNotificationToUser(player: String, opponents: String, win: Boolean): Flow[F, Unit] =
    for {
      userDto    <- arbiterClient.findPlayer(player.toLowerCase)
      botResponse = BotResponse(userDto.user.tid.getOrElse(-1), IdGen.msgId, createOutput(opponents, win))
      _          <- cmdPublisher.publishToBot(botResponse)(userDto.user.active && botResponse.chatId > 0)
    } yield ()

  private def createOutput(opponents: String, win: Boolean): String = {
    val action = if (win) "win" else "lose"
    s"$PREFIX Your $action against $opponents was successfully stored $SUFFIX"
  }
}
object AddRoundPostProcessor {
  def apply[F[_]](implicit ev: AddRoundPostProcessor[F]): AddRoundPostProcessor[F] = ev
  def impl[F[_]: MonadThrowable: Logger](
    ac: ArbiterClient[F],
    cmdPublisher: PublisherProxy[F]
  ): AddRoundPostProcessor[F] =
    new AddRoundPostProcessor[F](ac, cmdPublisher)
}
