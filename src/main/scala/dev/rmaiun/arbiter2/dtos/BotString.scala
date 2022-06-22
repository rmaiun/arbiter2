package dev.rmaiun.arbiter2.dtos

import dev.rmaiun.arbiter2.utils.Constants.{ PREFIX, SUFFIX }

case class BotString(value: String) {}
object BotString {
  def apply(value: String): String = s"$PREFIX$value$SUFFIX"
}
