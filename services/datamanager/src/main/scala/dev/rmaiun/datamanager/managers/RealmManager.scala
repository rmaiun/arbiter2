package dev.rmaiun.datamanager.managers

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.datamanager.db.entities.Realm
import dev.rmaiun.datamanager.dtos.api.RealmDtoSet._
import dev.rmaiun.datamanager.helpers.DtoMapper.realmToDto
import dev.rmaiun.datamanager.services.{ AlgorithmService, RealmService }
import dev.rmaiun.flowtypes.Flow.Flow
import io.chrisdavenport.log4cats.Logger

trait RealmManager[F[_]] {
  def registerRealm(dtoIn: RegisterRealmDtoIn): Flow[F, RegisterRealmDtoOut]
  def updateRealmAlgorithm(dtoIn: UpdateRealmAlgorithmDtoIn): Flow[F, UpdateRealmAlgorithmDtoOut]
  def findRealm(name: GetRealmDtoIn): Flow[F, GetRealmDtoOut]
}

object RealmManager {
  def apply[F[_]](implicit ev: RealmManager[F]): RealmManager[F] = ev
  def impl[F[_]: Monad: Logger: Sync](
    realmService: RealmService[F],
    algorithmService: AlgorithmService[F]
  ): RealmManager[F] = new RealmManager[F] {
    override def registerRealm(dtoIn: RegisterRealmDtoIn): Flow[F, RegisterRealmDtoOut] =
      for {
        algorithm <- algorithmService.getAlgorithmByName(dtoIn.algorithm)
        stored    <- realmService.create(Realm(0, dtoIn.realmName, dtoIn.teamSize, algorithm.id))
      } yield RegisterRealmDtoOut(realmToDto(stored, Some(algorithm.value)))

    override def updateRealmAlgorithm(dtoIn: UpdateRealmAlgorithmDtoIn): Flow[F, UpdateRealmAlgorithmDtoOut] =
      for {
        algorithm <- algorithmService.getAlgorithmByName(dtoIn.algorithm)
        realm     <- realmService.get(dtoIn.id)
        updRealm  <- realmService.update(realm.copy(selectedAlgorithm = algorithm.id))
      } yield UpdateRealmAlgorithmDtoOut(realmToDto(updRealm, Some(algorithm.value)))

    override def findRealm(dtoIn: GetRealmDtoIn): Flow[F, GetRealmDtoOut] =
      for {
        realm     <- realmService.getByName(dtoIn.realm)
        algorithm <- algorithmService.getAlgorithmById(realm.selectedAlgorithm)
      } yield GetRealmDtoOut(realmToDto(realm, Some(algorithm.value)))
  }
}
