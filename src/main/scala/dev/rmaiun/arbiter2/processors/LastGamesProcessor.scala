package dev.rmaiun.arbiter2.processors

import cats.Monad
import dev.rmaiun.arbiter2.commands.LastGamesCmd
import dev.rmaiun.arbiter2.dtos.{ BotRequest, BotResponse, Definition, ProcessorResponse }
import dev.rmaiun.arbiter2.managers.GameManager
import dev.rmaiun.arbiter2.utils.{ Constants, IdGen }
import dev.rmaiun.common.SeasonHelper
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.arbiter2.dtos.CmdType.LAST_GAMES_CMD
import dev.rmaiun.arbiter2.dtos._
import dev.rmaiun.arbiter2.utils.Constants.{ LINE_SEPARATOR, _ }
import dev.rmaiun.arbiter2.utils.{ Constants, IdGen }
import dev.rmaiun.protocol.http.GameDtoSet.{ ListGameHistoryDtoIn, StoredGameHistoryDto }

case class LastGamesProcessor[F[_]: Monad](gameManager: GameManager[F]) extends Processor[F] {

  override def definition: Definition = Definition.query(LAST_GAMES_CMD)

  override def process(input: BotRequest): Flow[F, Option[ProcessorResponse]] =
    for {
      dto         <- parseDto[LastGamesCmd](input.data)
      season       = dto.season.getOrElse(SeasonHelper.currentSeason)
      historyList <- loadHistory(season)
    } yield {
      val result      = format(historyList, season)
      val botResponse = BotResponse(input.chatId, IdGen.msgId, result)
      Some(ProcessorResponse.ok(botResponse))
    }

  private def loadHistory(season: String): Flow[F, List[StoredGameHistoryDto]] =
    gameManager.listGameHistory(ListGameHistoryDtoIn(Constants.defaultRealm, season)).map { dtoOut =>
      if (dtoOut.games.size <= 10) {
        dtoOut.games
      } else {
        val from = dtoOut.games.size - 11
        dtoOut.games.zipWithIndex.filter(_._2 >= from).map(_._1)
      }
    }

  private def format(dto: List[StoredGameHistoryDto], season: String): String = {
    val result = dto match {
      case Nil => s"No games found for season $season"
      case _ =>
        dto
          .map(formatRound)
          .mkString(LINE_SEPARATOR)
    }
    val separator = "-" * 30
    s"""Last 10 games
       |$separator
       |$result""".stripMargin.toBotMsg
  }

  private def formatRound(dto: StoredGameHistoryDto): String = {
    val winners = s"${dto.w1.capitalize}/${dto.w2.capitalize}"
    val losers  = s"${dto.l1.capitalize}/${dto.l2.capitalize}"
    val shutout = if (dto.shutout) "(✓)" else ""
    s"$winners vs $losers $shutout"
  }
}

object LastGamesProcessor {
  def apply[F[_]](implicit ev: LastGamesProcessor[F]): LastGamesProcessor[F] = ev
  def impl[F[_]: Monad](gameManager: GameManager[F]): LastGamesProcessor[F] =
    new LastGamesProcessor[F](gameManager)
}
