package dev.rmaiun.datamanager.dtos.api

import dev.rmaiun.datamanager.dtos.api.RealmDtoSet._
import dev.rmaiun.datamanager.dtos.api.UserDtoSet._
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

object codec {
  implicit val RealmDtoEncoder: Encoder[RealmDto] = deriveEncoder[RealmDto]
  implicit val RealmDtoDecoder: Decoder[RealmDto] = deriveDecoder[RealmDto]

  implicit val UserDataEncoder: Encoder[UserData] = deriveEncoder[UserData]
  implicit val UserDataDecoder: Decoder[UserData] = deriveDecoder[UserData]

  implicit val RegisterRealmDtoInEncoder: Encoder[RegisterRealmDtoIn] = deriveEncoder[RegisterRealmDtoIn]
  implicit val RegisterRealmDtoInDecoder: Decoder[RegisterRealmDtoIn] = deriveDecoder[RegisterRealmDtoIn]

  implicit val RegisterRealmDtoOutEncoder: Encoder[RegisterRealmDtoOut] = deriveEncoder[RegisterRealmDtoOut]
  implicit val RegisterRealmDtoOutDecoder: Decoder[RegisterRealmDtoOut] = deriveDecoder[RegisterRealmDtoOut]

  implicit val UpdateRealmAlgorithmDtoInEncoder: Encoder[UpdateRealmAlgorithmDtoIn] =
    deriveEncoder[UpdateRealmAlgorithmDtoIn]
  implicit val UpdateRealmAlgorithmDtoInDecoder: Decoder[UpdateRealmAlgorithmDtoIn] =
    deriveDecoder[UpdateRealmAlgorithmDtoIn]

  implicit val UpdateRealmAlgorithmDtoOutEncoder: Encoder[UpdateRealmAlgorithmDtoOut] =
    deriveEncoder[UpdateRealmAlgorithmDtoOut]
  implicit val UpdateRealmAlgorithmDtoOutDecoder: Decoder[UpdateRealmAlgorithmDtoOut] =
    deriveDecoder[UpdateRealmAlgorithmDtoOut]

  implicit val GetRealmDtoInEncoder: Encoder[GetRealmDtoIn] = deriveEncoder[GetRealmDtoIn]
  implicit val GetRealmDtoInDecoder: Decoder[GetRealmDtoIn] = deriveDecoder[GetRealmDtoIn]

  implicit val GetRealmDtoOutEncoder: Encoder[GetRealmDtoOut] = deriveEncoder[GetRealmDtoOut]
  implicit val GetRealmDtoOutDecoder: Decoder[GetRealmDtoOut] = deriveDecoder[GetRealmDtoOut]

//  user codec
  implicit val UserDtoEncoder: Encoder[UserDto] = deriveEncoder[UserDto]
  implicit val UserDtoDecoder: Decoder[UserDto] = deriveDecoder[UserDto]

  implicit val RegisterUserDtoInEncoder: Encoder[RegisterUserDtoIn] = deriveEncoder[RegisterUserDtoIn]
  implicit val RegisterUserDtoInDecoder: Decoder[RegisterUserDtoIn] = deriveDecoder[RegisterUserDtoIn]

  implicit val RegisterUserDtoOutEncoder: Encoder[RegisterUserDtoOut] = deriveEncoder[RegisterUserDtoOut]
  implicit val RegisterUserDtoOutDecoder: Decoder[RegisterUserDtoOut] = deriveDecoder[RegisterUserDtoOut]

  implicit val FindAllUsersDtoInEncoder: Encoder[FindAllUsersDtoIn] = deriveEncoder[FindAllUsersDtoIn]
  implicit val FindAllUsersDtoInDecoder: Decoder[FindAllUsersDtoIn] = deriveDecoder[FindAllUsersDtoIn]

  implicit val FindAllUsersDtoOutEncoder: Encoder[FindAllUsersDtoOut] = deriveEncoder[FindAllUsersDtoOut]
  implicit val FindAllUsersDtoOutDecoder: Decoder[FindAllUsersDtoOut] = deriveDecoder[FindAllUsersDtoOut]

  implicit val FindUserDtoInEncoder: Encoder[FindUserDtoIn] = deriveEncoder[FindUserDtoIn]
  implicit val FindUserDtoInDecoder: Decoder[FindUserDtoIn] = deriveDecoder[FindUserDtoIn]

  implicit val FindUserDtoOutEncoder: Encoder[FindUserDtoOut] = deriveEncoder[FindUserDtoOut]
  implicit val FindUserDtoOutDecoder: Decoder[FindUserDtoOut] = deriveDecoder[FindUserDtoOut]

  implicit val AssignUserToRealmDtoInEncoder: Encoder[AssignUserToRealmDtoIn] = deriveEncoder[AssignUserToRealmDtoIn]
  implicit val AssignUserToRealmDtoInDecoder: Decoder[AssignUserToRealmDtoIn] = deriveDecoder[AssignUserToRealmDtoIn]

  implicit val AssignUserToRealmDtoOutEncoder: Encoder[AssignUserToRealmDtoOut] = deriveEncoder[AssignUserToRealmDtoOut]
  implicit val AssignUserToRealmDtoOutDecoder: Decoder[AssignUserToRealmDtoOut] = deriveDecoder[AssignUserToRealmDtoOut]

  implicit val SwitchActiveRealmDtoInEncoder: Encoder[SwitchActiveRealmDtoIn] = deriveEncoder[SwitchActiveRealmDtoIn]
  implicit val SwitchActiveRealmDtoInDecoder: Decoder[SwitchActiveRealmDtoIn] = deriveDecoder[SwitchActiveRealmDtoIn]

  implicit val SwitchActiveRealmDtoOutEncoder: Encoder[SwitchActiveRealmDtoOut] =
    deriveEncoder[SwitchActiveRealmDtoOut]
  implicit val SwitchActiveRealmDtoOutDecoder: Decoder[SwitchActiveRealmDtoOut] =
    deriveDecoder[SwitchActiveRealmDtoOut]

  implicit val ProcessActivationDtoInEncoder: Encoder[ProcessActivationDtoIn] = deriveEncoder[ProcessActivationDtoIn]
  implicit val ProcessActivationDtoInDecoder: Decoder[ProcessActivationDtoIn] = deriveDecoder[ProcessActivationDtoIn]

  implicit val ProcessActivationDtoOutEncoder: Encoder[ProcessActivationDtoOut] =
    deriveEncoder[ProcessActivationDtoOut]
  implicit val ProcessActivationDtoOutDecoder: Decoder[ProcessActivationDtoOut] =
    deriveDecoder[ProcessActivationDtoOut]

  implicit val LinkTidDtoInEncoder: Encoder[LinkTidDtoIn] = deriveEncoder[LinkTidDtoIn]
  implicit val LinkTidDtoInDecoder: Decoder[LinkTidDtoIn] = deriveDecoder[LinkTidDtoIn]

  implicit val LinkTidDtoOutEncoder: Encoder[LinkTidDtoOut] = deriveEncoder[LinkTidDtoOut]
  implicit val LinkTidDtoOutDecoder: Decoder[LinkTidDtoOut] = deriveDecoder[LinkTidDtoOut]

  implicit val FindAvailableRealmsDtoInEncoder: Encoder[FindAvailableRealmsDtoIn] =
    deriveEncoder[FindAvailableRealmsDtoIn]
  implicit val FindAvailableRealmsDtoInDecoder: Decoder[FindAvailableRealmsDtoIn] =
    deriveDecoder[FindAvailableRealmsDtoIn]

  implicit val FindAvailableRealmsDtoOutEncoder: Encoder[FindAvailableRealmsDtoOut] =
    deriveEncoder[FindAvailableRealmsDtoOut]
  implicit val FindAvailableRealmsDtoOutDecoder: Decoder[FindAvailableRealmsDtoOut] =
    deriveDecoder[FindAvailableRealmsDtoOut]

}
