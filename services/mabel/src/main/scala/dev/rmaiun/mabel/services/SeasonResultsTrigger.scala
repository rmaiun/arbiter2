package dev.rmaiun.mabel.services

import cats.syntax.apply._
import dev.profunktor.fs2rabbit.model.{AmqpMessage, AmqpProperties}
import dev.rmaiun.flowtypes.Flow.{Flow, MonadThrowable}
import dev.rmaiun.flowtypes.{FLog, Flow}
import dev.rmaiun.mabel.dtos.AmqpStructures.AmqpPublisher
import dev.rmaiun.mabel.dtos.CmdType.SEASON_RESULTS_CMD
import dev.rmaiun.mabel.dtos.{BotRequest, CmdType}
import dev.rmaiun.mabel.utils.{Constants, IdGen}
import dev.rmaiun.protocol.http.UserDtoSet.UserRoleData
import io.chrisdavenport.log4cats.Logger

class SeasonResultsTrigger[F[_]: MonadThrowable: Logger](arbiterClient: ArbiterClient[F], publisher: AmqpPublisher[F]) {
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
        val data    = BotRequest.BotRequestEncoder(request).toString()
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
  def impl[F[_]: MonadThrowable: Logger](ac: ArbiterClient[F], p: AmqpPublisher[F]): SeasonResultsTrigger[F] =
    new SeasonResultsTrigger[F](ac, p)
}
