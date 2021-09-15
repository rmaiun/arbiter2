package dev.rmaiun.datamanager.dtos.api

import dev.rmaiun.datamanager.dtos.api.RealmDtos.RealmDto

import java.time.{ ZoneOffset, ZonedDateTime }

object UserDtos {
  case class UserDto(
    id: Long,
    surname: String,
    nickname: Option[String] = None,
    tid: Option[Long] = None,
    active: Boolean = true,
    createdAt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
  )
  case class UserData(surname: String, tid: Option[Long] = None)

  case class RegisterUserDtoIn(user: UserData, moderatorTid: Long)
  case class RegisterUserDtoOut(user: UserDto)

  case class FindAllUsersDtoIn(realm: String, activeStatus: Option[Boolean])
  case class FindAllUsersDtoOut(items: List[UserDto])

  case class FindUserDtoIn(surname: Option[String] = None, tid: Option[Long] = None)
  case class FindUserDtoOut(user: UserDto)

  case class AssignUserToRealmDtoIn(
    user: String,
    realm: String,
    role: Option[String],
    switchAsActive: Option[Boolean],
    moderatorTid: Long
  )
  case class AssignUserToRealmDtoOut(
    user: String,
    realm: String,
    role: String,
    assignedAt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    switchedAsActive: Option[Boolean]
  )

  case class SwitchActiveRealmDtoIn(user: String, realm: String, moderatorTid: Long)
  case class SwitchActiveRealmDtoOut(activeRealm: String)

  case class ProcessActivationDtoIn(users: List[String], moderatorTid: Long, realm: String, activate: Boolean)
  case class ProcessActivationDtoOut(users: List[String], activationStatus: Boolean)

  case class LinkTidDtoIn(tid: Long, nameToLink: String, moderatorTid: Long, realm: String)
  case class LinkTidDtoOut(
    subscribedSurname: String,
    subscribedTid: Long,
    createdDateTime: ZonedDateTime
  )

  case class FindAvailableRealmsDtoIn(surname: String)
  case class FindAvailableRealmsDtoOut(availableRealms: List[RealmDto])
}
