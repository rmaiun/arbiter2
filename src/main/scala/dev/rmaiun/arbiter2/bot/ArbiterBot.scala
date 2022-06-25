package dev.rmaiun.arbiter2.bot

import cats.effect.Async
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import com.bot4s.telegram.models.Message
import dev.profunktor.fs2rabbit.model.AmqpMessage
import dev.rmaiun.arbiter2.bot.ParentBot._
import dev.rmaiun.arbiter2.commands.{ AddPlayerCmd, AddRoundCmd, LastGamesCmd, SeasonStatsCmd }
import dev.rmaiun.arbiter2.dtos.AmqpStructures
import dev.rmaiun.arbiter2.dtos.AmqpStructures.AmqpPublisher
import dev.rmaiun.arbiter2.dtos.CmdType._
import dev.rmaiun.arbiter2.helpers.ConfigProvider.BotConfig
import dev.rmaiun.common.QuarterCalculator
import dev.rmaiun.flowtypes.Flow.MonadThrowable

import scala.util.Try

class ArbiterBot[F[_]: Async: MonadThrowable](token: String, amqpStructures: AmqpStructures[F], cfg: BotConfig)
    extends ParentBot[F](token)
    with StartCommand
    with SelfCommand {

  onCommand(StartBotCmd) { implicit msg =>
    for {
      _   <- logCmdInvocation(StartBotCmd)
      res <- response(startCmdText(cfg.version))
    } yield res
  }

  onCommand(SelfBotCmd) { implicit msg =>
    for {
      _   <- logCmdInvocation(SelfBotCmd)
      res <- response(selfCmdText)
    } yield res
  }

  onCommand(AddRoundBotCmd) { implicit msg =>
    withArgs { args =>
      val clearArgs = args.map(_.trim)
      val moderator = msg.from.fold(-1L)(u => u.id)
      val shutout   = clearArgs.size == 5 && clearArgs.last == "-суха"
      if (args.size >= 4) {
        val dto     = AddRoundCmd(clearArgs.head, clearArgs(1), clearArgs(2), clearArgs(3), shutout, moderator)
        val request = botRequest(ADD_ROUND_CMD, dto)
        processCmd(AddRoundBotCmd, request.asMessage)(amqpStructures.botInPersistPublisher)
      } else {
        reply("Bad command arguments: expected: 'w1 w2 l1 l2 [-суха]'").void
      }
    }
  }

  onCommand(RegisterUserBotCmd) { implicit msg =>
    withArgs { args =>
      val clearArgs = args.map(_.trim)
      val moderator = msg.from.fold(-1L)(u => u.id)
      if (args.size == 2) {
        val tid     = Try(clearArgs(1).toLong).toOption
        val dto     = AddPlayerCmd(clearArgs.head.toLowerCase, tid, admin = false, moderator)
        val request = botRequest(ADD_PLAYER_CMD, dto)
        processCmd(RegisterUserBotCmd, request.asMessage)(amqpStructures.botInPersistPublisher)
      } else {
        reply("Bad command arguments: expected: 'surname tid'").void
      }
    }
  }

  onRegex("^Season Stats \uD83D\uDCC8".r) { implicit msg => _ =>
    val cmd     = SeasonStatsCmd(QuarterCalculator.currentQuarter)
    val request = botRequest(SHORT_STATS_CMD, cmd)
    processCmd(SeasonStatsButtonLabel, request.asMessage)(amqpStructures.botInPublisher)
  }

  onRegex("^Elo Rating \uD83D\uDDFF".r) { implicit msg => _ =>
    val request = botRequest(ELO_RATING_CMD)
    processCmd(EloRatingButtonLabel, request.asMessage)(amqpStructures.botInPublisher)
  }

  onRegex("^Last Games \uD83D\uDCCB".r) { implicit msg => _ =>
    val cmd     = LastGamesCmd(QuarterCalculator.currentQuarter.some)
    val request = botRequest(LAST_GAMES_CMD, cmd)
    processCmd(LastGamesButtonLabel, request.asMessage)(amqpStructures.botInPublisher)
  }

  private def processCmd(cmdType: String, data: AmqpMessage[String])(
    publisher: AmqpPublisher[F]
  )(implicit msg: Message): F[Unit] =
    for {
      _ <- publisher(data)
      _ <- logCmdInvocation(cmdType)
    } yield ()
}
