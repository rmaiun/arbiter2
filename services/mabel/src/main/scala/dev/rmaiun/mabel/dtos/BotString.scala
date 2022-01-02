package dev.rmaiun.mabel.dtos

import dev.rmaiun.mabel.utils.Constants.{ PREFIX, SUFFIX }

case class BotString(value: String) {}
object BotString {
  def apply(value: String): String = s"$PREFIX$value$SUFFIX"
}
