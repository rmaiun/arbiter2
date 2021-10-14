package dev.rmaiun.mabel.processors

import cats.Monad
import dev.rmaiun.common.{ DateFormatter, SeasonHelper }
import dev.rmaiun.flowtypes.FLog
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.commands.AddRoundCmd
import dev.rmaiun.mabel.commands.AddRoundCmd._
import dev.rmaiun.mabel.dtos.EloRatingDto.{ CalculatedPoints, EloPlayers, UserCalculatedPoints }
import dev.rmaiun.mabel.dtos.{ BotRequest, ProcessorResponse }
import dev.rmaiun.mabel.services.{ ArbiterClient, EloPointsCalculator, IdGenerator }
import dev.rmaiun.mabel.utils.Constants
import dev.rmaiun.mabel.utils.Constants.{ PREFIX, SUFFIX }
import dev.rmaiun.protocol.http.GameDtoSet._
import dev.rmaiun.protocol.http.UserDtoSet.FindUserDtoOut
import io.chrisdavenport.log4cats.Logger

case class AddRoundProcessor[F[_]: Monad: Logger](
  arbiterClient: ArbiterClient[F],
  eloPointsCalculator: EloPointsCalculator[F]
) extends Processor[F] {
  override def process(input: BotRequest): Flow[F, ProcessorResponse] =
    for {
      dto           <- parseDto[AddRoundCmd](input.data)
      w1            <- loadPlayer(dto.w1)
      w2            <- loadPlayer(dto.w2)
      l1            <- loadPlayer(dto.l1)
      l2            <- loadPlayer(dto.l2)
      userPoints    <- calculateEloPoints(w1, w2, l1, l2)
      pointsIdList  <- storeEloPoints(userPoints, dto.moderator)
      _             <- FLog.info(s"Elo points were successfully stored with id: ${pointsIdList.mkString("[", ",", "]")}")
      storedHistory <- storeHistory(dto)
    } yield {
      val msg = formatMessage(storedHistory.storedRound.id, storedHistory.storedRound.realm)
      ProcessorResponse.ok(input.chatId, IdGenerator.msgId, msg)
    }

  private def formatMessage(id: Long, realm: String): String =
    s"$PREFIX New game was stored with id $id for realm $realm $SUFFIX"

  private def storeHistory(dto: AddRoundCmd): Flow[F, AddGameHistoryDtoOut] = {
    val ghDto = GameHistoryDtoIn(
      Constants.defaultRealm,
      SeasonHelper.currentSeason,
      dto.w1.toLowerCase,
      dto.w2.toLowerCase,
      dto.l1.toLowerCase,
      dto.l2.toLowerCase,
      dto.shutout
    )
    arbiterClient.storeGameHistory(AddGameHistoryDtoIn(ghDto, dto.moderator))
  }
  private def loadPlayer(surname: String): Flow[F, FindUserDtoOut] =
    arbiterClient.findPlayer(surname.toLowerCase)

  private def calculateEloPoints(
    w1: FindUserDtoOut,
    w2: FindUserDtoOut,
    l1: FindUserDtoOut,
    l2: FindUserDtoOut
  ): Flow[F, UserCalculatedPoints] =
    eloPointsCalculator.calculate(EloPlayers(w1.user.surname, w2.user.surname, l1.user.surname, l2.user.surname))

  private def storeEloPoints(data: UserCalculatedPoints, moderatorTid: Long): Flow[F, List[Long]] = {
    val w1Dto = formDto(data.w1, moderatorTid)
    val w2Dto = formDto(data.w2, moderatorTid)
    val l1Dto = formDto(data.l1, moderatorTid)
    val l2Dto = formDto(data.l2, moderatorTid)
    for {
      out1 <- arbiterClient.storeEloPoints(w1Dto)
      out2 <- arbiterClient.storeEloPoints(w2Dto)
      out3 <- arbiterClient.storeEloPoints(l1Dto)
      out4 <- arbiterClient.storeEloPoints(l2Dto)
    } yield List(out1.id, out2.id, out3.id, out4.id)
  }

  private def formDto(dto: CalculatedPoints, moderatorTid: Long): AddEloPointsDtoIn =
    AddEloPointsDtoIn(EloPointsDto(dto.player, dto.points, DateFormatter.now), moderatorTid)
}
