package dev.rmaiun.datamanager.services

import dev.rmaiun.datamanager.dtos.api.UserDtos._
import dev.rmaiun.flowtypes.Flow.Flow

trait UserService[F[_]] {
  def findUser(dtoIn: FindUserDtoIn): Flow[F, FindUserDtoOut]
  def findAllUsers(dtoIn: FindAllUsersDtoIn): Flow[F, FindAllUsersDtoOut]
  def assignUserToRealm(dtoIn: AssignUserToRealmDtoIn): Flow[F, AssignUserToRealmDtoOut]
  def switchActiveRealm(dtoIn: SwitchActiveRealmDtoIn): Flow[F, SwitchActiveRealmDtoOut]
  def processActivation(dtoIn: ProcessActivationDtoIn): Flow[F, ProcessActivationDtoOut]
  def linkTid(dtoIn: LinkTidDtoIn): Flow[F, LinkTidDtoOut]
  def changeSubscriptionStatus(dtoIn: ChangeSubscriptionStatusDtoIn): Flow[F, ChangeSubscriptionStatusDtoOut]
  def findRelatedRealms(dtoIn: FindAvailableRealmsDtoIn): Flow[F, FindAvailableRealmsDtoOut]
}
