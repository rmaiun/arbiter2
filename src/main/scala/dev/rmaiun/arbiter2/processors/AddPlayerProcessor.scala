package dev.rmaiun.arbiter2.processors

import cats.effect.Sync
import dev.rmaiun.arbiter2.commands.AddPlayerCmd
import dev.rmaiun.arbiter2.commands.AddPlayerCmd._
import dev.rmaiun.arbiter2.dtos.CmdType.ADD_PLAYER_CMD
import dev.rmaiun.arbiter2.dtos.{ BotRequest, BotResponse, Definition, ProcessorResponse }
import dev.rmaiun.arbiter2.managers.UserManager
import dev.rmaiun.arbiter2.utils.Constants._
import dev.rmaiun.arbiter2.utils.{ Constants, IdGen }
import dev.rmaiun.flowtypes.FLog
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.protocol.http.UserDtoSet._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

case class AddPlayerProcessor[F[_]: Sync](userManager: UserManager[F]) extends Processor[F] {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)

  override def definition: Definition = Definition.persistence(ADD_PLAYER_CMD)

  override def process(input: BotRequest): Flow[F, Option[ProcessorResponse]] =
    for {
      dto             <- parseDto[AddPlayerCmd](input.data)
      addPlayerResult <- registerPlayer(dto)
      _               <- assignUserToRealm(dto)
      _               <- FLog.info(s"Registered new player ${addPlayerResult.user.surname} with id ${addPlayerResult.user.id}")
    } yield {
      val result      = s"New player was registered with id ${addPlayerResult.user.id}".toBotMsg
      val botResponse = BotResponse(input.chatId, IdGen.msgId, result)
      Some(ProcessorResponse.ok(botResponse))
    }

  private def registerPlayer(dto: AddPlayerCmd): Flow[F, RegisterUserDtoOut] = {
    val userData = UserData(dto.surname, dto.tid)
    userManager.registerUser(RegisterUserDtoIn(userData, dto.moderator))
  }

  private def assignUserToRealm(dto: AddPlayerCmd): Flow[F, AssignUserToRealmDtoOut] = {
    val role = if (dto.admin) Some("RealmAdmin") else None
    val requestDto =
      AssignUserToRealmDtoIn(dto.surname.toLowerCase, Constants.defaultRealm, role, Some(true), dto.moderator)
    userManager.assignUserToRealm(requestDto)
  }
}

object AddPlayerProcessor {
  def apply[F[_]](implicit ev: AddPlayerProcessor[F]): AddPlayerProcessor[F] = ev
  def impl[F[_]: Sync](userManager: UserManager[F]): AddPlayerProcessor[F] =
    new AddPlayerProcessor[F](userManager)
}
