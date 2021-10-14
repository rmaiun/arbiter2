package dev.rmaiun.mabel.services

object IdGenerator {
  def msgId: Int = (System.currentTimeMillis % Int.MaxValue).toInt
}
