package dev.rmaiun.mabel.dtos

sealed trait CmdType
object CmdType {
  case object Query       extends CmdType
  case object Persistence extends CmdType
  case object Internal    extends CmdType

  val ADD_PLAYER_CMD     = "addPlayer"
  val ADD_ROUND_CMD      = "addRound"
  val SHORT_STATS_CMD    = "shortStats"
  val ELO_RATING_CMD     = "eloRating"
  val LAST_GAMES_CMD     = "lastGames"
  val SEASON_RESULTS_CMD = "seasonResults"
  val BROADCAST_MSG_CMD = "broadcastMessage"
}
