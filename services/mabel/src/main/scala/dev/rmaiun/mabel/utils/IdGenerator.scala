package dev.rmaiun.mabel.utils

object IdGenerator {
  def msgId: Int = (System.currentTimeMillis % Int.MaxValue).toInt
}
