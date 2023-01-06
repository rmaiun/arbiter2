package dev.rmaiun.arbiter2.dtos

sealed trait CmdType
object CmdType {
  case object Query       extends CmdType
  case object Persistence extends CmdType
  case object Internal    extends CmdType

  val ADD_PLAYER_CMD     = "ADD_PLAYER_EVENT"
  val ADD_ROUND_CMD      = "ADD_ROUND_EVENT"
  val SHORT_STATS_CMD    = "SEASON_STATS_EVENT"
  val ELO_RATING_CMD     = "ELO_RATING_EVENT"
  val LAST_GAMES_CMD     = "LAST_GAMES_EVENT"
  val SEASON_RESULTS_CMD = "SEASON_RESULTS_EVENT"
  val BROADCAST_MSG_CMD  = "BROADCAST_MESSAGE_EVENT"
}
