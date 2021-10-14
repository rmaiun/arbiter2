package dev.rmaiun.mabel.dtos

case class ProcessorResponse(error: Boolean, botResponse: BotResponse)

object ProcessorResponse {
  def ok(chatId: Long, msgId: Int, result: String): ProcessorResponse =
    ProcessorResponse(error = false, BotResponse(chatId, msgId, result))

  def ok(botResponse: BotResponse): ProcessorResponse = ProcessorResponse(error = false, botResponse)

  def error(chatId: Long, msgId: Int, result: String): ProcessorResponse =
    ProcessorResponse(error = true, new BotResponse(chatId, msgId, result))

  def error(botResponse: BotResponse): ProcessorResponse = ProcessorResponse(error = true, botResponse)

}
