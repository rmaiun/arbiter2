package dev.rmaiun.arbiter2.processors

import cats.Monad
import cats.syntax.option._
import dev.rmaiun.arbiter2.dtos.{ BotRequest, Definition, ProcessorResponse }
import dev.rmaiun.arbiter2.helpers.ReportCache
import dev.rmaiun.arbiter2.helpers.ReportCache.EloRatingReport
import dev.rmaiun.arbiter2.managers.{ GameManager, UserManager }
import dev.rmaiun.arbiter2.utils.{ Constants, IdGen }
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.arbiter2.dtos.CmdType._
import dev.rmaiun.arbiter2.dtos.{ BotRequest, Definition, ProcessorResponse }
import ReportCache.EloRatingReport
import dev.rmaiun.arbiter2.managers.{ GameManager, UserManager }
import Constants._
import dev.rmaiun.arbiter2.utils.{ Constants, IdGen }
import dev.rmaiun.protocol.http.GameDtoSet.{ ListEloPointsDtoIn, ListEloPointsDtoOut }
import dev.rmaiun.protocol.http.UserDtoSet.FindAllUsersDtoIn

case class EloRatingProcessor[F[_]: Monad](
  gameManager: GameManager[F],
  userManager: UserManager[F],
  cache: ReportCache[F]
) extends Processor[F] {

  override def definition: Definition = Definition.query(ELO_RATING_CMD)

  override def process(input: BotRequest): Flow[F, Option[ProcessorResponse]] =
    cache.get(EloRatingReport).flatMap {
      case Some(v) => Flow.pure(Some(ProcessorResponse.ok(input.chatId, IdGen.msgId, v)))
      case None    => processInternal(input)
    }

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

    for {
      pr <- action
      _  <- cache.put(EloRatingReport, pr.map(_.botResponse.result))
    } yield pr
  }

  private def loadActiveUsers: Flow[F, List[String]] =
    userManager
      .findAllUsers(FindAllUsersDtoIn(Constants.defaultRealm, true.some))
      .map(_.items.map(i => i.surname.toLowerCase))

  private def loadEloPoints(users: List[String]): Flow[F, ListEloPointsDtoOut] =
    gameManager.listCalculatedEloPoints(ListEloPointsDtoIn(users.some))
}

object EloRatingProcessor {
  def apply[F[_]](implicit ev: EloRatingProcessor[F]): EloRatingProcessor[F] = ev
  def impl[F[_]: Monad](
    gameManager: GameManager[F],
    userManager: UserManager[F],
    cache: ReportCache[F]
  ): EloRatingProcessor[F] =
    new EloRatingProcessor[F](gameManager, userManager, cache)
}
