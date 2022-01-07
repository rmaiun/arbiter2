package dev.rmaiun.soos.services

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.soos.db.entities.Algorithm
import dev.rmaiun.soos.errors.AlgorithmErrors.AlgorithmNotFoundException
import dev.rmaiun.soos.repositories.AlgorithmRepo
import doobie.hikari.HikariTransactor
import doobie.implicits._

trait AlgorithmService[F[_]] {
  def getAlgorithmByName(algorithm: String): Flow[F, Algorithm]
  def getAlgorithmById(algorithmId: Long): Flow[F, Algorithm]
  def createAlgorithm(algorithm: Algorithm): Flow[F, Algorithm]
  def deleteAlgorithm(algorithmId: Long): Flow[F, Int]
}

object AlgorithmService {

  def apply[F[_]](implicit ev: AlgorithmService[F]): AlgorithmService[F] = ev

  def impl[F[_]: Monad: Sync](algorithmRepo: AlgorithmRepo[F], xa: HikariTransactor[F]): AlgorithmService[F] =
    new AlgorithmService[F] {
      override def getAlgorithmByName(algorithm: String): Flow[F, Algorithm] = {
        val algorithmResult = for {
          maybeAlg <- algorithmRepo.getByName(algorithm).transact(xa).attemptSql.adaptError
        } yield maybeAlg
        algorithmResult.flatMap {
          case Some(alg) => Flow.pure(alg)
          case None      => Flow.error(AlgorithmNotFoundException(Map("algorithm" -> s"$algorithm")))
        }
      }

      override def getAlgorithmById(id: Long): Flow[F, Algorithm] = {
        val algorithm = for {
          maybeAlg <- algorithmRepo.getById(id).transact(xa).attemptSql.adaptError
        } yield maybeAlg
        algorithm.flatMap {
          case Some(alg) => Flow.pure(alg)
          case None      => Flow.error(AlgorithmNotFoundException(Map("algorithmId" -> s"$id")))
        }
      }

      override def createAlgorithm(algorithm: Algorithm): Flow[F, Algorithm] =
        algorithmRepo.create(algorithm).transact(xa).attemptSql.adaptError

      override def deleteAlgorithm(id: Long): Flow[F, Int] =
        algorithmRepo.removeN(List(id)).transact(xa).attemptSql.adaptError
    }
}
