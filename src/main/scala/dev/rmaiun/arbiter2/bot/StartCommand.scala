package dev.rmaiun.arbiter2.bot

import dev.rmaiun.arbiter2.bot.ParentBot._

trait StartCommand {
  def startCmdText(version: String): String =
    s"""
        in progress ...
       |--------------------------------------
       |           version:$version
                    """.stripMargin
}
