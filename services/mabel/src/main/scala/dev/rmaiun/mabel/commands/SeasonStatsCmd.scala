package dev.rmaiun.mabel.commands

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

case class SeasonStatsCmd(season: String)

object SeasonStatsCmd {
  implicit val SeasonStatsCmdDecoder: Decoder[SeasonStatsCmd] = deriveDecoder[SeasonStatsCmd]
  implicit val SeasonStatsCmdEncoder: Encoder[SeasonStatsCmd] = deriveEncoder[SeasonStatsCmd]
}
