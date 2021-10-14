package dev.rmaiun.mabel.processors

import cats.Monad
import dev.rmaiun.flowtypes.FLog
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.commands.AddPlayerCmd
import dev.rmaiun.mabel.commands.AddPlayerCmd._
import dev.rmaiun.mabel.dtos.{ BotRequest, BotResponse, ProcessorResponse }
import dev.rmaiun.mabel.services.{ ArbiterClient, IdGenerator }
import dev.rmaiun.mabel.utils.Constants
import dev.rmaiun.mabel.utils.Constants.{ PREFIX, SUFFIX }
import dev.rmaiun.protocol.http.UserDtoSet._
import io.chrisdavenport.log4cats.Logger

case class AddPlayerProcessor[F[_]: Monad: Logger](arbiterClient: ArbiterClient[F]) extends Processor[F] {

  override def process(input: BotRequest): Flow[F, ProcessorResponse] =
    for {
      dto             <- parseDto[AddPlayerCmd](input.data)
      addPlayerResult <- registerPlayer(dto)
      _               <- assignUserToRealm(dto)
      _               <- FLog.info(s"Registered new player ${addPlayerResult.user.surname} with id ${addPlayerResult.user.id}")
    } yield {
      val result      = s"$PREFIX New player was registered with id ${addPlayerResult.user.id} $SUFFIX"
      val botResponse = BotResponse(input.chatId, IdGenerator.msgId, result)
      ProcessorResponse.ok(botResponse)
    }

  private def registerPlayer(dto: AddPlayerCmd): Flow[F, RegisterUserDtoOut] = {
    val userData = UserData(dto.surname, Some(dto.tid))
    arbiterClient.addPlayer(RegisterUserDtoIn(userData, dto.moderator))
  }

  private def assignUserToRealm(dto: AddPlayerCmd): Flow[F, AssignUserToRealmDtoOut] = {
    val role = if (dto.admin) Some("RealmAdmin") else None
    val requestDto =
      AssignUserToRealmDtoIn(dto.surname.toLowerCase, Constants.defaultRealm, role, Some(true), dto.moderator)
    arbiterClient.assignUserToRealm(requestDto)
  }
}
