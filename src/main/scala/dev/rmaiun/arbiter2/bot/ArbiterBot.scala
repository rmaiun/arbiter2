package dev.rmaiun.arbiter2.bot

import cats.effect.{Async, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import dev.rmaiun.arbiter2.bot.ParentBot._
import dev.rmaiun.arbiter2.commands.SeasonStatsCmd
import dev.rmaiun.arbiter2.dtos.AmqpStructures
import dev.rmaiun.arbiter2.dtos.CmdType._
import dev.rmaiun.common.QuarterCalculator
import dev.rmaiun.flowtypes.Flow.MonadThrowable
class ArbiterBot[F[_]: Async: MonadThrowable](token: String, amqpStructures: AmqpStructures[F])
    extends ParentBot[F](token) {

  onRegex("""^Season Stats \uD83D\uDCC8""".r) { implicit msg => _ =>
    val cmd     = SeasonStatsCmd(QuarterCalculator.currentQuarter)
    val request = botRequest(SHORT_STATS_CMD, cmd)
    for {
      _ <- Sync[F].delay(amqpStructures.botInPublisher(request.asMessage))
      _ <- logCmdInvocation(ParentBot.StatsCmd)
    } yield ()
  }

  onCommand("/bla") { implicit msg =>
    withArgs { args =>
      println(args)
      reply("ok").void
    }
  }

}
