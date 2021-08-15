package dev.rmaiun.datamanager.routes

import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Monad}
import dev.rmaiun.datamanager.dtos.AlgorithmDtos.{CreateAlgorithmDtoIn, GetAlgorithmDtoIn}
import dev.rmaiun.datamanager.dtos.RealmDtos.{GetRealmDtoIn, RegisterRealmDtoIn}
import dev.rmaiun.datamanager.services.{AlgorithmService, RealmService}
import dev.rmaiun.errorhandling.errors.AppRuntimeException
import dev.rmaiun.errorhandling.errors.codec._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import io.chrisdavenport.log4cats.Logger
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, Response}
object DataManagerRoutes {

  implicit def errorEntityEncoder[F[_]: Applicative, T: Encoder]: EntityEncoder[F, T] = jsonEncoderOf[F, T]
  implicit def errorEntityDecoder[F[_]: Sync, T: Decoder]: EntityDecoder[F, T]        = jsonOf[F, T]

  def flowToResponse[F[_]: Sync: Monad: Logger, T](
    flow: Flow[F, T]
  )(implicit ee: EntityEncoder[F, T]): F[Response[F]] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    Monad[F].flatMap(flow.value) {
      case Left(err) =>
        err match {
          case e: AppRuntimeException =>
            for {
              _ <- Logger[F].error(err)("FLow ends with AppRuntimeException")
              x <- Response[F](status = BadRequest).withEntity(ErrorDtoOut(e.code, e.message, e.app, e.params)).pure[F]
            } yield x
          case e: Throwable =>
            for {
              _ <- Logger[F].error(err)("FLow ends with Throwable")
              x <- Response[F](status = BadRequest)
                     .withEntity(ErrorDtoOut("systemException", e.getMessage, Some("datamanager")))
                     .pure[F]
            } yield x
        }
      case Right(value) => Ok(value)
    }
  }

  def algorithmRoutes[F[_]: Sync: Monad: Logger](algorithmService: AlgorithmService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dev.rmaiun.datamanager.dtos.AlgorithmDtos.codec._
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "find" / name =>
        flowToResponse(algorithmService.getAlgorithmByName(GetAlgorithmDtoIn(name)))

      case req @ POST -> Root / "create" =>
        val dtoOut = for {
          dtoIn <- Flow.fromF(req.as[CreateAlgorithmDtoIn])
          res   <- algorithmService.createAlgorithm(dtoIn)
        } yield res
        flowToResponse(dtoOut)
    }
  }

  def realmRoutes[F[_]: Sync: Monad: Logger](realmService: RealmService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dev.rmaiun.datamanager.dtos.RealmDtos.codec._
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "find" / name =>
        flowToResponse(realmService.getRealm(GetRealmDtoIn(name)))

      case req @ POST -> Root / "create" =>
        val dtoOut = for {
          dtoIn <- Flow.fromF(req.as[RegisterRealmDtoIn])
          res   <- realmService.registerRealm(dtoIn)
        } yield res
        flowToResponse(dtoOut)
    }
  }
}
