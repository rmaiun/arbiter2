package dev.rmaiun.mabel.processors
import cats.Monad
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.dtos.CmdType._
import dev.rmaiun.mabel.dtos.{BotRequest, Definition, ProcessorResponse}
import dev.rmaiun.mabel.services.ArbiterClient
import dev.rmaiun.mabel.utils.Constants.{LINE_SEPARATOR, PREFIX, SUFFIX}
import dev.rmaiun.mabel.utils.IdGen
import dev.rmaiun.protocol.http.GameDtoSet.ListEloPointsDtoOut
import io.chrisdavenport.log4cats.Logger

case class EloRatingProcessor[F[_]: Monad](ac: ArbiterClient[F]) extends Processor[F] {

  override def definition: Definition = Definition.query(ELO_RATING_CMD)

  override def process(input: BotRequest): Flow[F, Option[ProcessorResponse]] =
    for {
      users  <- loadActiveUsers
      points <- loadEloPoints(users)
    } yield {
      val separator = "-" * 30
      val playersRating = points.calculatedEloPoints
        .filter(_.gamesPlayed >= 30)
        .sortBy(-_.value)
        .zipWithIndex
        .map(e => s"${e._2 + 1}. ${e._1.user.capitalize} ${e._1.value}")
        .mkString(LINE_SEPARATOR)
      val msg = PREFIX + s"""Global Rating:
                            |$separator
                            |$playersRating
                            |$SUFFIX""".stripMargin
      Some(ProcessorResponse.ok(input.chatId, IdGen.msgId, msg))
    }

  private def loadActiveUsers: Flow[F, List[String]] =
    ac.findAllPlayers.map(_.items.map(i => i.surname.toLowerCase))

  private def loadEloPoints(users: List[String]): Flow[F, ListEloPointsDtoOut] =
    ac.listCalculatedEloPoints(users)
}

object EloRatingProcessor {
  def apply[F[_]](implicit ev: EloRatingProcessor[F]): EloRatingProcessor[F] = ev
  def impl[F[_]: Monad: Logger](ac: ArbiterClient[F]): EloRatingProcessor[F] =
    new EloRatingProcessor[F](ac)
}
