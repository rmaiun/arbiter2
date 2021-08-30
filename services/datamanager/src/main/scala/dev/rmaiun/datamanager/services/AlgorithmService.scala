package dev.rmaiun.datamanager.services

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.datamanager.db.entities.Algorithm
import dev.rmaiun.datamanager.dtos.api.AlgorithmDtos._
import dev.rmaiun.datamanager.errors.AlgorithmErrors.AlgorithmNotFoundException
import dev.rmaiun.datamanager.repositories.AlgorithmRepo
import dev.rmaiun.datamanager.validations.AlgorithmValidationSet._
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.validation.Validator
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.chrisdavenport.log4cats.Logger

trait AlgorithmService[F[_]] {
  def getAlgorithmByName(dtoIn: GetAlgorithmDtoIn): Flow[F, GetAlgorithmDtoOut]
  def getAlgorithmById(id: Long): Flow[F, GetAlgorithmDtoOut]
  def createAlgorithm(dtoIn: CreateAlgorithmDtoIn): Flow[F, CreateAlgorithmDtoOut]
  def deleteAlgorithm(dtoIn: DeleteAlgorithmDtoIn): Flow[F, DeleteAlgorithmDtoOut]
}

object AlgorithmService {

  def apply[F[_]](implicit ev: AlgorithmService[F]): AlgorithmService[F] = ev

  def impl[F[_]: Monad: Logger: Sync](algorithmRepo: AlgorithmRepo[F], xa: HikariTransactor[F]): AlgorithmService[F] =
    new AlgorithmService[F] {
      override def getAlgorithmByName(dtoIn: GetAlgorithmDtoIn): Flow[F, GetAlgorithmDtoOut] = {
        val algorithm = for {
          _        <- Validator.validateDto[F, GetAlgorithmDtoIn](dtoIn)
          maybeAlg <- algorithmRepo.getByName(dtoIn.algorithm).transact(xa).attemptSql.adaptError
        } yield maybeAlg
        algorithm.flatMap {
          case Some(alg) => Flow.pure(GetAlgorithmDtoOut(alg.id, alg.value))
          case None      => Flow.error(AlgorithmNotFoundException(Map("algorithm" -> s"${dtoIn.algorithm}")))
        }
      }

      override def getAlgorithmById(id: Long): Flow[F, GetAlgorithmDtoOut] = {
        val algorithm = for {
          maybeAlg <- algorithmRepo.getById(id).transact(xa).attemptSql.adaptError
        } yield maybeAlg
        algorithm.flatMap {
          case Some(alg) => Flow.pure(GetAlgorithmDtoOut(alg.id, alg.value))
          case None      => Flow.error(AlgorithmNotFoundException(Map("algorithmId" -> s"${id}")))
        }
      }

      override def createAlgorithm(dtoIn: CreateAlgorithmDtoIn): Flow[F, CreateAlgorithmDtoOut] =
        for {
          _   <- Validator.validateDto[F, CreateAlgorithmDtoIn](dtoIn)
          alg <- algorithmRepo.create(Algorithm(0, dtoIn.algorithm)).transact(xa).attemptSql.adaptError
        } yield CreateAlgorithmDtoOut(alg.id, alg.value)

      override def deleteAlgorithm(dtoIn: DeleteAlgorithmDtoIn): Flow[F, DeleteAlgorithmDtoOut] =
        for {
          _ <- Validator.validateDto[F, DeleteAlgorithmDtoIn](dtoIn)
          n <- algorithmRepo.removeN(List(dtoIn.id)).transact(xa).attemptSql.adaptError
        } yield DeleteAlgorithmDtoOut(dtoIn.id, n)
    }
}
