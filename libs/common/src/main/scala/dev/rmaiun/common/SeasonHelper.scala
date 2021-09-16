package dev.rmaiun.common

import java.time.{ LocalDate, Month }

object SeasonHelper {

  def currentSeason: String = {
    val now   = DateFormatter.now()
    val month = now.getMonth.getValue
    val year  = now.getYear
    val q     = quarterNum(month)
    s"S$q|$year"
  }

  def seasonGate(season: String): (LocalDate, LocalDate) = {
    val seasonData     = season.split("\\|")
    val qNum           = seasonData(0).split("")(1).toInt
    val (qStart, qEnd) = quarterGate(qNum)
    val year           = seasonData(1).toInt
    val start          = LocalDate.of(year, qStart, 1)
    val lastMonth      = Month.of(qEnd)
    val tmpDate        = LocalDate.of(year, lastMonth, 1)
    val end            = LocalDate.of(year, lastMonth, tmpDate.lengthOfMonth())
    (start, end)
  }

  private def quarterNum(month: Int): Int =
    if (month <= 3) 1
    else if (month <= 6) 2
    else if (month <= 9) 3
    else 4

  private def quarterGate(quarter: Int) = quarter match {
    case 1 => (1, 3)
    case 2 => (4, 6)
    case 3 => (7, 9)
    case 4 => (10, 12)
    case _ => (0, 0)
  }
}
