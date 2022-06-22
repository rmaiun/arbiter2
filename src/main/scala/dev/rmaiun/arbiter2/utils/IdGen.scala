package dev.rmaiun.arbiter2.utils

object IdGen {
  def msgId: Int = (System.currentTimeMillis % Int.MaxValue).toInt
}
