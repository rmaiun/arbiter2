package dev.rmaiun.common

import java.time.LocalDateTime

object QuarterCalculator {
  def currentQuarter: String =
    createQuarter(LocalDateTime.now())

  def createQuarter(date: LocalDateTime): String = {
    val month = date.getMonth.getValue
    val quarter =
      if (List(1, 2, 3).contains(month)) 1
      else if (List(4, 5, 6).contains(month)) 2
      else if (List(7, 8, 9).contains(month)) 3
      else 4
    s"S$quarter|${date.getYear}"
  }
}
