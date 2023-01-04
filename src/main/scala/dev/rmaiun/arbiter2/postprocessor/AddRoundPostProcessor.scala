package dev.rmaiun.arbiter2.postprocessor

import cats.syntax.foldable._
import cats.syntax.option._
import dev.rmaiun.arbiter2.commands.AddRoundCmd
import dev.rmaiun.arbiter2.dtos.CmdType.ADD_ROUND_CMD
import dev.rmaiun.arbiter2.dtos.{ BotRequest, BotResponse, Definition, GameHistoryCriteria }
import dev.rmaiun.arbiter2.helpers.PublisherProxy
import dev.rmaiun.arbiter2.managers.UserManager
import dev.rmaiun.arbiter2.services.GameService
import dev.rmaiun.arbiter2.utils.Constants._
import dev.rmaiun.arbiter2.utils.IdGen
import dev.rmaiun.common.SeasonHelper.currentSeason
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.protocol.http.UserDtoSet._
import io.circe.Json

case class AddRoundPostProcessor[F[_]: MonadThrowable](
  userManager: UserManager[F],
  gameService: GameService[F],
  cmdPublisher: PublisherProxy[F]
) extends PostProcessor[F] {

  override def definition: Definition = Definition.internal(ADD_ROUND_CMD)

  override def postProcess(input: BotRequest): Flow[F, Unit] =
    for {
      cmd <- parseDto[AddRoundCmd](input.data)
      _   <- sendNotificationToUser(cmd.w1, s"${cmd.l1.capitalize}/${cmd.l2.capitalize}", win = true)
      _   <- sendNotificationToUser(cmd.w2, s"${cmd.l1.capitalize}/${cmd.l2.capitalize}", win = true)
      _   <- sendNotificationToUser(cmd.l1, s"${cmd.w1.capitalize}/${cmd.w2.capitalize}", win = false)
      _   <- sendNotificationToUser(cmd.l2, s"${cmd.w1.capitalize}/${cmd.w2.capitalize}", win = false)
      _   <- notifyRealmAdmins(cmd)
      _   <- notifySeasonParticipantsWithShutout(input.data)
    } yield ()

  private def notifySeasonParticipantsWithShutout(data: Option[Json]): Flow[F, Unit] =
    parseDto[AddRoundCmd](data).flatMap(dto =>
      if (dto.shutout) {
        val season = Some(currentSeason)
        gameService.listHistoryByCriteria(GameHistoryCriteria(defaultRealm, season, Some(true))).flatMap {
          listHistory =>
            if (listHistory.size == 1) {
              for {
                users <- userManager.findAllUsers(FindAllUsersDtoIn(defaultRealm, Some(true), season))
                _     <- users.items.map(u => sendShutoutNotificationToUser(u, dto.l1, dto.l2)).sequence_
              } yield ()
            } else {
              Flow.unit
            }
        }
      } else {
        Flow.unit
      }
    )

  private def sendShutoutNotificationToUser(user: UserDto, loser1: String, loser2: String): Flow[F, Unit] = {
    val msg         = s"""Great plan, can't go wrong.
                 |${loser1.capitalize} and ${loser2.capitalize} lost a first shutout in this season.
                 |Your moms would be proud of you for that one.
              """.stripMargin.toBotMsg
    val botResponse = BotResponse(user.tid.getOrElse(-1), IdGen.msgId, msg)
    cmdPublisher.publishToBot(botResponse)(user.active && botResponse.chatId > 0)
  }

  private def sendNotificationToUser(player: String, opponents: String, win: Boolean): Flow[F, Unit] =
    for {
      userDto    <- userManager.findUser(FindUserDtoIn(player.toLowerCase.some))
      botResponse = BotResponse(userDto.user.tid.getOrElse(-1), IdGen.msgId, createOutput(opponents, win))
      _          <- cmdPublisher.publishToBot(botResponse)(userDto.user.active && botResponse.chatId > 0)
    } yield ()

  private def notifyRealmAdmins(cmd: AddRoundCmd): Flow[F, Unit] =
    for {
      adminsDto <- userManager.findRealmAdmins(FindRealmAdminsDtoIn(defaultRealm))
      _         <- sendMsgToAdmins(cmd, adminsWithoutModeratorAndPlayers(cmd, adminsDto.adminUsers))
    } yield ()

  private def sendMsgToAdmins(cmd: AddRoundCmd, admins: List[UserRoleData]): Flow[F, Unit] = {
    val shutout = if (cmd.shutout) "(✓)" else ""
    val msg =
      s"""Round 
         |${cmd.w1.capitalize}/${cmd.w2.capitalize} vs ${cmd.l1.capitalize}/${cmd.l2.capitalize} $shutout
         |was stored
         """.stripMargin.toBotMsg
    admins
      .map(d => BotResponse(d.tid.getOrElse(0), IdGen.msgId, msg))
      .map(response => cmdPublisher.publishToBot(response))
      .sequence_
  }

  private def adminsWithoutModeratorAndPlayers(cmd: AddRoundCmd, data: List[UserRoleData]): List[UserRoleData] = {
    val affectedPlayers = Set(cmd.w1.toLowerCase, cmd.w2.toLowerCase, cmd.l1.toLowerCase, cmd.l2.toLowerCase)
    data
      .filter(urd => urd.tid.fold(false)(tid => tid != cmd.moderator))
      .filter(urd => !affectedPlayers.contains(urd.surname.toLowerCase))
  }

  private def createOutput(opponents: String, win: Boolean): String = {
    val action = if (win) "win" else "lose"
    s"Your $action against $opponents was successfully stored".toBotMsg
  }
}
object AddRoundPostProcessor {
  def apply[F[_]](implicit ev: AddRoundPostProcessor[F]): AddRoundPostProcessor[F] = ev
  def impl[F[_]: MonadThrowable](
    userManager: UserManager[F],
    gameService: GameService[F],
    cmdPublisher: PublisherProxy[F]
  ): AddRoundPostProcessor[F] =
    new AddRoundPostProcessor[F](userManager, gameService, cmdPublisher)
}
