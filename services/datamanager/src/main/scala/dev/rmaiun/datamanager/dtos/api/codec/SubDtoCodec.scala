package dev.rmaiun.datamanager.dtos.api.codec
import dev.rmaiun.datamanager.dtos.api.GameDtoSet.{CalculatedEloPointsDto, EloPointsDto, GameHistoryDto}
import dev.rmaiun.datamanager.dtos.api.RealmDtoSet.RealmDto
import dev.rmaiun.datamanager.dtos.api.UserDtoSet.{RealmShortInfo, UserData, UserDto}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
trait SubDtoCodec {
  implicit val RealmDtoEncoder: Encoder[RealmDto] = deriveEncoder[RealmDto]
  implicit val RealmDtoDecoder: Decoder[RealmDto] = deriveDecoder[RealmDto]

  implicit val UserDataEncoder: Encoder[UserData] = deriveEncoder[UserData]
  implicit val UserDataDecoder: Decoder[UserData] = deriveDecoder[UserData]

  implicit val UserDtoEncoder: Encoder[UserDto] = deriveEncoder[UserDto]
  implicit val UserDtoDecoder: Decoder[UserDto] = deriveDecoder[UserDto]

  implicit val GameHistoryDtoEncoder: Encoder[GameHistoryDto] = deriveEncoder[GameHistoryDto]
  implicit val GameHistoryDtoDecoder: Decoder[GameHistoryDto] = deriveDecoder[GameHistoryDto]

  implicit val EloPointsDtoEncoder: Encoder[EloPointsDto] = deriveEncoder[EloPointsDto]
  implicit val EloPointsDtoDecoder: Decoder[EloPointsDto] = deriveDecoder[EloPointsDto]

  implicit val CalculatedEloPointsDtoEncoder: Encoder[CalculatedEloPointsDto] = deriveEncoder[CalculatedEloPointsDto]
  implicit val CalculatedEloPointsDtoDecoder: Decoder[CalculatedEloPointsDto] = deriveDecoder[CalculatedEloPointsDto]

  implicit val RealmShortInfoEncoder: Encoder[RealmShortInfo] = deriveEncoder[RealmShortInfo]
  implicit val RealmShortInfoDecoder: Decoder[RealmShortInfo] = deriveDecoder[RealmShortInfo]

}
