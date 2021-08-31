package dev.rmaiun.datamanager.services

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.datamanager.db.entities.Realm
import dev.rmaiun.datamanager.dtos.api.AlgorithmDtos.GetAlgorithmDtoIn
import dev.rmaiun.datamanager.dtos.api.RealmDtos._
import dev.rmaiun.datamanager.errors.RealmErrors.RealmNotFoundRuntimeException
import dev.rmaiun.datamanager.helpers.DtoMapper.realmToDto
import dev.rmaiun.datamanager.repositories.RealmRepo
import dev.rmaiun.datamanager.validations.RealmValidationSet._
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.validation.Validator
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.chrisdavenport.log4cats.Logger

trait RealmService[F[_]] {
  def registerRealm(dtoIn: RegisterRealmDtoIn): Flow[F, RegisterRealmDtoOut]
  def updateRealmAlgorithm(dtoIn: UpdateRealmAlgorithmDtoIn): Flow[F, UpdateRealmAlgorithmDtoOut]
  def getRealm(name: GetRealmDtoIn): Flow[F, GetRealmDtoOut]
  def dropRealm(dtoIn: DropRealmDtoIn): Flow[F, DropRealmDtoOut]
  def findRealmsByUser(surname: String): Flow[F, List[Realm]]
}

object RealmService {
  def apply[F[_]](implicit ev: RealmService[F]): RealmService[F] = ev

  def impl[F[_]: Monad: Logger: Sync](
    realmRepo: RealmRepo[F],
    algorithmService: AlgorithmService[F],
    xa: HikariTransactor[F]
  ): RealmService[F] = new RealmService[F] {
    override def registerRealm(dtoIn: RegisterRealmDtoIn): Flow[F, RegisterRealmDtoOut] =
      for {
        _         <- Validator.validateDto[F, RegisterRealmDtoIn](dtoIn)
        algorithm <- algorithmService.getAlgorithmByName(GetAlgorithmDtoIn(dtoIn.algorithm))
        stored    <- createRealm(Realm(0, dtoIn.realmName, dtoIn.teamSize, algorithm.id))
      } yield RegisterRealmDtoOut(realmToDto(stored, Some(algorithm.algorithm)))

    override def updateRealmAlgorithm(dtoIn: UpdateRealmAlgorithmDtoIn): Flow[F, UpdateRealmAlgorithmDtoOut] =
      for {
        _         <- Validator.validateDto[F, UpdateRealmAlgorithmDtoIn](dtoIn)
        algorithm <- algorithmService.getAlgorithmByName(GetAlgorithmDtoIn(dtoIn.algorithm))
        realm     <- getRealm(dtoIn.id)
        updRealm  <- updateRealm(realm.copy(selectedAlgorithm = algorithm.id))
      } yield UpdateRealmAlgorithmDtoOut(realmToDto(updRealm, Some(algorithm.algorithm)))

    override def dropRealm(dtoIn: DropRealmDtoIn): Flow[F, DropRealmDtoOut] =
      for {
        _ <- Validator.validateDto[F, DropRealmDtoIn](dtoIn)
        n <- removeRealms(dtoIn.id :: Nil)
      } yield DropRealmDtoOut(dtoIn.id, n)

    override def getRealm(dtoIn: GetRealmDtoIn): Flow[F, GetRealmDtoOut] = {
      val findRealm = realmRepo.getByName(dtoIn.realm).transact(xa).attemptSql.adaptError.flatMap {
        case Some(r) => Flow.pure(r)
        case None    => Flow.error(RealmNotFoundRuntimeException(Map("name" -> s"${dtoIn.realm}")))
      }
      for {
        _            <- Validator.validateDto[F, GetRealmDtoIn](dtoIn)
        realm        <- findRealm
        algorithmDto <- algorithmService.getAlgorithmById(realm.selectedAlgorithm)
      } yield GetRealmDtoOut(realmToDto(realm, Some(algorithmDto.algorithm)))
    }

    private def updateRealm(realm: Realm): Flow[F, Realm] =
      realmRepo.update(realm).transact(xa).attemptSql.adaptError

    private def createRealm(realm: Realm): Flow[F, Realm] =
      realmRepo.create(realm).transact(xa).attemptSql.adaptError

    private def getRealm(id: Long): Flow[F, Realm] =
      realmRepo.getById(id).transact(xa).attemptSql.adaptError.flatMap {
        case Some(value) => Flow.pure(value)
        case None        => Flow.error(RealmNotFoundRuntimeException(Map("id" -> s"$id")))
      }

    private def removeRealms(idList: List[Long]): Flow[F, Int] =
      realmRepo.removeN(idList).transact(xa).attemptSql.adaptError

    override def findRealmsByUser(surname: String): Flow[F, List[Realm]] =
      realmRepo.listRealmsByUser(surname).transact(xa).attemptSql.adaptError
  }
}
