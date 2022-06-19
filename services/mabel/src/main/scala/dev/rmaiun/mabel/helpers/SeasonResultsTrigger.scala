package dev.rmaiun.mabel.helpers

import cats.effect.Sync
import cats.syntax.apply._
import dev.profunktor.fs2rabbit.model.{AmqpMessage, AmqpProperties}
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{FLog, Flow}
import dev.rmaiun.mabel.dtos.AmqpStructures.AmqpPublisher
import dev.rmaiun.mabel.dtos.BotRequest
import dev.rmaiun.mabel.dtos.CmdType.SEASON_RESULTS_CMD
import dev.rmaiun.mabel.utils.{Constants, IdGen}
import dev.rmaiun.protocol.http.UserDtoSet.UserRoleData
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class SeasonResultsTrigger[F[_]: Sync](arbiterClient: ArbiterClient[F], publisher: AmqpPublisher[F]) {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)

  def run(): Flow[F, Unit] =
    for {
      _      <- FLog.info("Trigger season results distribution")
      dtoOut <- arbiterClient.findRealmAdmins()
      _      <- sendMessage(dtoOut.adminUsers)
    } yield ()

  private def sendMessage(adminUsers: List[UserRoleData]): Flow[F, Unit] = {
    val owner = adminUsers.filter(_.role == Constants.ownerRole).head
    owner.tid match {
      case Some(v) =>
        val request = BotRequest(SEASON_RESULTS_CMD, IdGen.msgId, v, owner.surname.capitalize)
        val data    = BotRequest.BotRequestCodec(request).toString()
        val cmd     = AmqpMessage(data, AmqpProperties())
        Flow.effect(publisher(cmd))
      case None =>
        FLog.warn(s"Not tid found for ${owner.surname.capitalize}") *>
          Flow.unit
    }
  }
}

object SeasonResultsTrigger {
  def apply[F[_]](implicit ev: SeasonResultsTrigger[F]): SeasonResultsTrigger[F] = ev
  def impl[F[_]: Sync](ac: ArbiterClient[F], p: AmqpPublisher[F]): SeasonResultsTrigger[F] =
    new SeasonResultsTrigger[F](ac, p)
}
