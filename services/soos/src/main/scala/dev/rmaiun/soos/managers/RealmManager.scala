package dev.rmaiun.soos.managers
import cats.Monad
import cats.effect.Sync
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.protocol.http.RealmDtoSet._
import dev.rmaiun.soos.db.entities.Realm
import dev.rmaiun.soos.helpers.DtoMapper.realmToDto
import dev.rmaiun.soos.services.{ AlgorithmService, RealmService, UserService }

trait RealmManager[F[_]] {
  def registerRealm(dtoIn: RegisterRealmDtoIn): Flow[F, RegisterRealmDtoOut]
  def updateRealmAlgorithm(dtoIn: UpdateRealmAlgorithmDtoIn): Flow[F, UpdateRealmAlgorithmDtoOut]
  def findRealm(name: GetRealmDtoIn): Flow[F, GetRealmDtoOut]
}

object RealmManager {
  def apply[F[_]](implicit ev: RealmManager[F]): RealmManager[F] = ev
  def impl[F[_]: Monad: Sync](
    realmService: RealmService[F],
    algorithmService: AlgorithmService[F],
    userService: UserService[F]
  ): RealmManager[F] = new RealmManager[F] {
    override def registerRealm(dtoIn: RegisterRealmDtoIn): Flow[F, RegisterRealmDtoOut] =
      for {
        algorithm <- algorithmService.getAlgorithmByName(dtoIn.algorithm)
        stored    <- realmService.create(Realm(0, dtoIn.realmName, dtoIn.teamSize, algorithm.id))
        _         <- userService.assignToRealm(stored.id, 1, 1)
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
