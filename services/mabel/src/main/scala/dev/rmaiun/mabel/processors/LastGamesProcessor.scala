package dev.rmaiun.mabel.processors

import cats.Monad
import dev.rmaiun.common.SeasonHelper
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.commands.LastGamesCmd
import dev.rmaiun.mabel.dtos.CmdType.LAST_GAMES_CMD
import dev.rmaiun.mabel.dtos.{ BotRequest, BotResponse, Definition, ProcessorResponse }
import dev.rmaiun.mabel.services.ArbiterClient
import dev.rmaiun.mabel.utils.Constants.{ LINE_SEPARATOR, PREFIX, SUFFIX }
import dev.rmaiun.mabel.utils.{ Constants, IdGen }
import dev.rmaiun.protocol.http.GameDtoSet.StoredGameHistoryDto
import io.chrisdavenport.log4cats.Logger

case class LastGamesProcessor[F[_]: Monad: Logger](arbiterClient: ArbiterClient[F]) extends Processor[F] {

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
    arbiterClient.listGameHistory(Constants.defaultRealm, season).map { dtoOut =>
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
    PREFIX +
      s"""Last 10 games
         |$separator
         |$result
         |$SUFFIX""".stripMargin
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
  def impl[F[_]: Monad: Logger](ac: ArbiterClient[F]): LastGamesProcessor[F] =
    new LastGamesProcessor[F](ac)
}
