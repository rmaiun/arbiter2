package dev.rmaiun.mabel.commands

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class LastGamesCmd(season: Option[String] = None)
object LastGamesCmd {
  implicit val LastGamesCmdDecoder: Decoder[LastGamesCmd] = deriveDecoder[LastGamesCmd]
  implicit val LastGamesCmdEncoder: Encoder[LastGamesCmd] = deriveEncoder[LastGamesCmd]
}