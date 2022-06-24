package dev.rmaiun.protocol.http.codec

import dev.rmaiun.protocol.http.GameDtoSet.{ CalculatedEloPointsDto, EloPointsDto, GameHistoryDto }
import dev.rmaiun.protocol.http.RealmDtoSet.RealmDto
import dev.rmaiun.protocol.http.SeasonDtoSet.SeasonDto
import dev.rmaiun.protocol.http.UserDtoSet.{ RealmShortInfo, UserData, UserDto, UserRoleData }
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
trait SubDtoCodec {
  implicit val RealmDtoCodec: Codec[RealmDto] = deriveCodec[RealmDto]

  implicit val UserDataCodec: Codec[UserData] = deriveCodec[UserData]

  implicit val UserRoleDataCodec: Codec[UserRoleData] = deriveCodec[UserRoleData]

  implicit val UserDtoCodec: Codec[UserDto] = deriveCodec[UserDto]

  implicit val GameHistoryDtoCodec: Codec[GameHistoryDto] = deriveCodec[GameHistoryDto]

  implicit val EloPointsDtoCodec: Codec[EloPointsDto] = deriveCodec[EloPointsDto]

  implicit val CalculatedEloPointsDtoCodec: Codec[CalculatedEloPointsDto] = deriveCodec[CalculatedEloPointsDto]

  implicit val RealmShortInfoCodec: Codec[RealmShortInfo] = deriveCodec[RealmShortInfo]

  implicit val SeasonDtoCodec: Codec[SeasonDto] = deriveCodec[SeasonDto]
}
