package dev.rmaiun.mabel.utils

object Constants {
  val LINE_SEPARATOR: String = System.lineSeparator
  val DELIMITER: String      = "-" * 34
  val SUFFIX: String         = "```"
  val PREFIX: String         = SUFFIX + LINE_SEPARATOR
  val DEFAULT_RESULT: String = ""

  val defaultRealm: String = "ua_foosball"
  val expectedGames        = 20

  implicit class RichBotMsg(val msg:String) extends AnyVal {
    def toBotMsg: String = PREFIX + msg + SUFFIX
  }
}
