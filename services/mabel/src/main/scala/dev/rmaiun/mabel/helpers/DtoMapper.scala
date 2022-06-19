package dev.rmaiun.mabel.helpers

import dev.rmaiun.mabel.db.entities.{Realm, User}
import dev.rmaiun.protocol.http.RealmDtoSet.RealmDto
import dev.rmaiun.protocol.http.UserDtoSet.UserDto

object DtoMapper {
  def userToDto(u: User): UserDto = UserDto(u.id, u.surname, u.nickname, u.tid, u.active, u.createdAt)

  def realmToDto(realm: Realm, algorithm: Option[String] = None): RealmDto =
    RealmDto(realm.id, realm.name, algorithm, realm.teamSize)
}
