package dev.rmaiun.arbiter2.bot

import cats.effect.kernel.{Async, MonadCancelThrow, Sync}
import cats.implicits._
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import com.bot4s.telegram.methods.{ParseMode, SendPhoto}
import com.bot4s.telegram.models._
import com.typesafe.scalalogging.Logger
import dev.profunktor.fs2rabbit.model.{AmqpEnvelope, AmqpMessage, AmqpProperties}
import dev.rmaiun.arbiter2.bot.ParentBot.{EloRatingButtonLabel, LastGamesButtonLabel, SeasonStatsButtonLabel}
import dev.rmaiun.arbiter2.dtos.{BotRequest, BotResponse}
import dev.rmaiun.flowtypes.Flow.MonadThrowable
import io.circe.Codec
import io.circe.parser._
import org.asynchttpclient.Dsl.asyncHttpClient
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

import java.nio.file.{Files, Paths}
import java.util.Date

object ParentBot {
  val StartBotCmd                  = "/start"
  val SelfBotCmd                   = "/self"
  val AddRoundBotCmd               = "/add"
  val RegisterUserBotCmd           = "/register"
  val BroadcastMsgBotCmd           = "/bc"
  val BroadcastMsgInTestModeBotCmd = "/bct"

  var SeasonStatsButtonLabel = "Season Stats \uD83D\uDCC8"
  var EloRatingButtonLabel   = "Elo Rating \uD83D\uDDFF"
  var LastGamesButtonLabel   = "Last Games \uD83D\uDCCB"

  val NotAvailable = "n/a"
  val Error        = "*ERROR*:"

  implicit class RichBotRequest(br: BotRequest) {
    def asMessage: AmqpMessage[String] = new AmqpMessage(BotRequest.BotRequestCodec(br).toString(), AmqpProperties())
  }

}

abstract class ParentBot[F[_]: Async: MonadThrowable](token: String)
    extends TelegramBot[F](token, AsyncHttpClientCatsBackend.usingClient[F](asyncHttpClient()))
    with Polling[F]
    with Commands[F]
    with RegexCommands[F] {
  val log: Logger = Logger("BOT")

  //  override val webhookUrl: String = ""
  //  override val port: Int = 8443

  def defaultMarkup(): Option[ReplyKeyboardMarkup] = {
    val seasonStatsButton = KeyboardButton(SeasonStatsButtonLabel)
    val eloRatingButton   = KeyboardButton(EloRatingButtonLabel)
    val lastGamesButton   = KeyboardButton(LastGamesButtonLabel)

    val markup = ReplyKeyboardMarkup(
      Seq(Seq(seasonStatsButton, eloRatingButton), Seq(lastGamesButton)),
      true.some,
      false.some,
      None,
      false.some
    )
    Some(markup)
  }

  def response(result: String)(implicit msg: Message): F[Unit] =
    replyWithMenu(result)

  def replyWithMenu(text: String)(implicit message: Message): F[Unit] =
    reply(
      text,
      parseMode = Some(ParseMode.Markdown),
      replyMarkup = defaultMarkup()
    ).void

  def logCmdInvocation(cmd: String)(implicit msg: Message): F[Unit] =
    Sync[F].delay(log.info(s"$cmd was invoked by ${msg.from.fold("Incognito")(x => s"${x.firstName}(${x.id})")})"))

  def botRequest[T](cmdType: String, cmd: T)(implicit msg: Message, codec: Codec[T]): BotRequest = BotRequest(
    cmdType,
    msg.chat.id,
    msg.from.fold(0L)(f => f.id),
    msg.from.fold(ParentBot.NotAvailable)(f => f.firstName),
    Some(codec(cmd))
  )
  def botRequest(cmdType: String)(implicit msg: Message): BotRequest = BotRequest(
    cmdType,
    msg.chat.id,
    msg.from.fold(0L)(f => f.id),
    msg.from.fold(ParentBot.NotAvailable)(f => f.firstName)
  )

  def processResponse(amqpEnvelope: AmqpEnvelope[String]): F[Unit] = {
    val botResponseEither = for {
      json <- parse(amqpEnvelope.payload)
      dto  <- BotResponse.BotResponseCodec.decodeJson(json)
    } yield dto

    botResponseEither match {
      case Left(err) =>
        Sync[F].delay(log.error("Problem with BotResponse parsing", err))
      case Right(dto) =>
        val msg = Message(
          dto.msgId,
          chat = Chat(dto.chatId, `type` = ChatType.Private),
          text = Some(dto.result),
          date = (new Date().getTime / 1000).toInt
        )
        val url = getClass.getResource("/bot/2022-01-11_09-32.png")
        val binary = SendPhoto(
          ChatId(dto.chatId),
          InputFile("testpicture.png", Files.readAllBytes(Paths.get(url.getPath.toString))),
          parseMode = Some(ParseMode.Markdown),
          replyMarkup = defaultMarkup()
        )
        MonadCancelThrow[F].recover(request(binary).void)(err =>
          Sync[F].delay(log.error("Failed to deliver message", err))
        )
    }
  }
}
