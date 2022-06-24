package dev.rmaiun.arbiter2.bot

import java.time.{ ZoneId, ZonedDateTime }
import com.bot4s.telegram.models.Message
import dev.rmaiun.arbiter2.bot.ParentBot.NotAvailable

trait SelfCommand {
  def selfCmdText(implicit msg: Message): String =
    s"""
       | User info:
       | messageId = ${msg.messageId}
       | chatId = ${msg.chat.id}
       | userId = ${msg.from.fold(NotAvailable)(x => x.id.toString)}
       | firstName = ${msg.from.fold(NotAvailable)(x => x.firstName)}
       | lastName = ${msg.from.flatMap(x => x.lastName).fold(NotAvailable)(x => x)}
       | username = ${msg.from.flatMap(x => x.username).fold(NotAvailable)(x => x)}
       | serverH = ${ZonedDateTime.now().getHour}
       | serverTS = ${ZonedDateTime.now().toString}
       | serverUaH = ${ZonedDateTime.now(ZoneId.of("Europe/Kiev")).getHour}
       | serverUaTS = ${ZonedDateTime.now(ZoneId.of("Europe/Kiev"))}""".stripMargin
}
