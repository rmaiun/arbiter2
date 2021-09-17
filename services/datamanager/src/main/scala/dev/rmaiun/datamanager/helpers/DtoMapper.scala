package dev.rmaiun.datamanager.helpers

import dev.rmaiun.datamanager.db.entities.{GameHistory, Realm, User}
import dev.rmaiun.datamanager.dtos.api.GameDtoSet.GameHistoryDto
import dev.rmaiun.datamanager.dtos.api.RealmDtoSet.RealmDto
import dev.rmaiun.datamanager.dtos.api.UserDtoSet.UserDto

object DtoMapper {
  def userToDto(u: User): UserDto = UserDto(u.id, u.surname, u.nickname, u.tid, u.active, u.createdAt)

  def realmToDto(realm: Realm, algorithm: Option[String] = None): RealmDto =
    RealmDto(realm.id, realm.name, algorithm, realm.teamSize)
}
