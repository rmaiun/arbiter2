package dev.rmaiun.protocol.http.codec
import dev.rmaiun.protocol.http.UserDtoSet._
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import io.circe.{Codec, Decoder, Encoder}

trait UserDtoCodec extends SubDtoCodec {
  implicit val RegisterUserDtoInCodec: Codec[RegisterUserDtoIn] = deriveCodec[RegisterUserDtoIn]

  implicit val RegisterUserDtoOutCodec: Codec[RegisterUserDtoOut] = deriveCodec[RegisterUserDtoOut]

  implicit val FindAllUsersDtoInCodec: Codec[FindAllUsersDtoIn] = deriveCodec[FindAllUsersDtoIn]

  implicit val FindAllUsersDtoOutCodec: Codec[FindAllUsersDtoOut] = deriveCodec[FindAllUsersDtoOut]

  implicit val FindUserDtoInCodec: Codec[FindUserDtoIn] = deriveCodec[FindUserDtoIn]

  implicit val FindUserDtoOutCodec: Codec[FindUserDtoOut] = deriveCodec[FindUserDtoOut]

  implicit val AssignUserToRealmDtoInCodec: Codec[AssignUserToRealmDtoIn] = deriveCodec[AssignUserToRealmDtoIn]

  implicit val AssignUserToRealmDtoOutCodec: Codec[AssignUserToRealmDtoOut] = deriveCodec[AssignUserToRealmDtoOut]

  implicit val SwitchActiveRealmDtoInCodec: Codec[SwitchActiveRealmDtoIn] = deriveCodec[SwitchActiveRealmDtoIn]

  implicit val SwitchActiveRealmDtoOutCodec: Codec[SwitchActiveRealmDtoOut] =
    deriveCodec[SwitchActiveRealmDtoOut]

  implicit val ProcessActivationDtoInCodec: Codec[ProcessActivationDtoIn] = deriveCodec[ProcessActivationDtoIn]

  implicit val ProcessActivationDtoOutCodec: Codec[ProcessActivationDtoOut] =
    deriveCodec[ProcessActivationDtoOut]

  implicit val LinkTidDtoInCodec: Codec[LinkTidDtoIn] = deriveCodec[LinkTidDtoIn]

  implicit val LinkTidDtoOutCodec: Codec[LinkTidDtoOut] = deriveCodec[LinkTidDtoOut]

  implicit val FindAvailableRealmsDtoInCodec: Codec[FindAvailableRealmsDtoIn] =
    deriveCodec[FindAvailableRealmsDtoIn]

  implicit val FindAvailableRealmsDtoOutCodec: Codec[FindAvailableRealmsDtoOut] =
    deriveCodec[FindAvailableRealmsDtoOut]

  implicit val FindRealmAdminsDtoInCodec: Codec[FindRealmAdminsDtoIn] = deriveCodec[FindRealmAdminsDtoIn]

  implicit val FindRealmAdminsDtoOutCodec: Codec[FindRealmAdminsDtoOut] = deriveCodec[FindRealmAdminsDtoOut]

}
