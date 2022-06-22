package dev.rmaiun.arbiter2.dtos.stats

case class SeasonShortStats(
  season: String,
  playersRating: List[PlayerStats],
  unrankedStats: List[UnrankedStats],
  gamesPlayed: Int,
  daysToSeasonEnd: Int,
  bestStreak: Option[Streak],
  worstStreak: Option[Streak]
)
