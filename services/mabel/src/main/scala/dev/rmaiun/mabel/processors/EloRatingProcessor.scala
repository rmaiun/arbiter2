package dev.rmaiun.mabel.processors
import cats.Monad
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.dtos.CmdType._
import dev.rmaiun.mabel.dtos.{ BotRequest, Definition, ProcessorResponse }
import dev.rmaiun.mabel.services.ReportCache.EloRatingReport
import dev.rmaiun.mabel.services.{ ArbiterClient, ReportCache }
import dev.rmaiun.mabel.utils.Constants.{ LINE_SEPARATOR, _ }
import dev.rmaiun.mabel.utils.IdGen
import dev.rmaiun.protocol.http.GameDtoSet.ListEloPointsDtoOut

case class EloRatingProcessor[F[_]: Monad](ac: ArbiterClient[F], cache: ReportCache[F]) extends Processor[F] {

  override def definition: Definition = Definition.query(ELO_RATING_CMD)

  override def process(input: BotRequest): Flow[F, Option[ProcessorResponse]] =
    cache.find(EloRatingReport)(processInternal(input))
//    processInternal(input)

  private def processInternal(input: BotRequest): Flow[F, Option[ProcessorResponse]] = {
    val action = for {
      users  <- loadActiveUsers
      points <- loadEloPoints(users)
    } yield {
      val separator = "-" * 30
      val playersRating = points.calculatedEloPoints
        .filter(_.gamesPlayed >= 30)
        .sortBy(-_.value)
        .zipWithIndex
        .map(e => s"${e._2 + 1}. ${e._1.user.capitalize} ${e._1.value}") match {
        case Nil         => "Rating is not formed yet"
        case players @ _ => players.mkString(LINE_SEPARATOR)
      }
      val msg = s"""Global Rating:
                   |$separator
                   |$playersRating""".stripMargin.toBotMsg
      Some(ProcessorResponse.ok(input.chatId, IdGen.msgId, msg))
    }
    action.flatMap(pr => cache.put(EloRatingReport, pr))
  }

  private def loadActiveUsers: Flow[F, List[String]] =
    ac.findAllPlayers.map(_.items.map(i => i.surname.toLowerCase))

  private def loadEloPoints(users: List[String]): Flow[F, ListEloPointsDtoOut] =
    ac.listCalculatedEloPoints(users)
}

object EloRatingProcessor {
  def apply[F[_]](implicit ev: EloRatingProcessor[F]): EloRatingProcessor[F] = ev
  def impl[F[_]: Monad](ac: ArbiterClient[F], cache: ReportCache[F]): EloRatingProcessor[F] =
    new EloRatingProcessor[F](ac, cache)
}
