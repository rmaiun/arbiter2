package dev.rmaiun.protocol.http

object RealmDtoSet {
  case class RealmDto(id: Long, name: String, selectedAlgorithm: Option[String], teamSize: Int)

  case class RegisterRealmDtoIn(realmName: String, algorithm: String, teamSize: Int)
  case class RegisterRealmDtoOut(realm: RealmDto)

  case class UpdateRealmAlgorithmDtoIn(id: Long, algorithm: String)
  case class UpdateRealmAlgorithmDtoOut(realm: RealmDto)

  case class GetRealmDtoIn(realm: String)
  case class GetRealmDtoOut(realm: RealmDto)

}
