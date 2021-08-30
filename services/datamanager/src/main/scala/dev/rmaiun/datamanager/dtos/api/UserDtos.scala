package dev.rmaiun.datamanager.dtos.api

import dev.rmaiun.datamanager.dtos.api.RealmDtos.RealmDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.{ZoneOffset, ZonedDateTime}

object UserDtos {
  case class UserDto(
    id: Long,
    surname: String,
    nickname: Option[String] = None,
    tid: Option[Long] = None,
    active: Boolean = true,
    createdAt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
  )

  case class RegisterUserDtoIn(surname: String, nickname: Option[String] = None, tid: Option[Long] = None)
  case class RegisterUserDtoOut(user: UserDto)

  case class FindAllUsersDtoIn(realm: String, activeStatus: Option[Boolean])
  case class FindAllUsersDtoOut(items: List[UserDto])

  case class FindUserDtoIn(surname: Option[String] = None, tid: Option[Long] = None)
  case class FindUserDtoOut(user: UserDto)

  case class AssignUserToRealmDtoIn(user: String, realm: String, role: Option[String], switchAsActive: Option[Boolean])
  case class AssignUserToRealmDtoOut(
    user: String,
    realm: String,
    role: String,
    assignedAt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    switchedAsActive: Option[Boolean]
  )

  case class SwitchActiveRealmDtoIn(user: String, realm: String)
  case class SwitchActiveRealmDtoOut(activeRealm: String)

  case class ProcessActivationDtoIn(users: List[String], moderatorTid: Long, realm:String, activate: Boolean)
  case class ProcessActivationDtoOut(users: List[UserDto])

  case class LinkTidDtoIn(tid: Long, nameToLink: String, moderatorTid: Long)
  case class LinkTidDtoOut(
    subscribedSurname: String,
    subscribedTid: Long,
    createdDateTime: ZonedDateTime,
    notificationsEnabled: Boolean
  )

  case class ChangeSubscriptionStatusDtoIn(enableSubscriptions: Boolean, tid: Long)
  case class ChangeSubscriptionStatusDtoOut(
    subscribedSurname: String,
    createdDateTime: ZonedDateTime,
    notificationsEnabled: Boolean
  )

  case class FindAvailableRealmsDtoIn(surname: String)
  case class FindAvailableRealmsDtoOut(availableRealms: List[RealmDto])

  object codec {
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

    implicit val AssignUserToRealmDtoOutEncoder: Encoder[SwitchActiveRealmDtoIn] = deriveEncoder[SwitchActiveRealmDtoIn]
    implicit val AssignUserToRealmDtoOutDecoder: Decoder[SwitchActiveRealmDtoIn] = deriveDecoder[SwitchActiveRealmDtoIn]

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

    implicit val ChangeSubscriptionStatusDtoInEncoder: Encoder[ChangeSubscriptionStatusDtoIn] =
      deriveEncoder[ChangeSubscriptionStatusDtoIn]
    implicit val ChangeSubscriptionStatusDtoInDecoder: Decoder[ChangeSubscriptionStatusDtoIn] =
      deriveDecoder[ChangeSubscriptionStatusDtoIn]

    implicit val ChangeSubscriptionStatusDtoOutEncoder: Encoder[ChangeSubscriptionStatusDtoOut] =
      deriveEncoder[ChangeSubscriptionStatusDtoOut]
    implicit val ChangeSubscriptionStatusDtoOutDecoder: Decoder[ChangeSubscriptionStatusDtoOut] =
      deriveDecoder[ChangeSubscriptionStatusDtoOut]

    implicit val FindAvailableRealmsDtoInEncoder: Encoder[FindAvailableRealmsDtoIn] =
      deriveEncoder[FindAvailableRealmsDtoIn]
    implicit val FindAvailableRealmsDtoInDecoder: Decoder[FindAvailableRealmsDtoIn] =
      deriveDecoder[FindAvailableRealmsDtoIn]

    implicit val FindAvailableRealmsDtoOutEncoder: Encoder[FindAvailableRealmsDtoOut] =
      deriveEncoder[FindAvailableRealmsDtoOut]
    implicit val FindAvailableRealmsDtoOutDecoder: Decoder[FindAvailableRealmsDtoOut] =
      deriveDecoder[FindAvailableRealmsDtoOut]
  }
}
