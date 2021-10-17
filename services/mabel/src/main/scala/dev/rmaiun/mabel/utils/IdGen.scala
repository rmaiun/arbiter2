package dev.rmaiun.mabel.utils

object IdGen {
  def msgId: Int = (System.currentTimeMillis % Int.MaxValue).toInt
}
