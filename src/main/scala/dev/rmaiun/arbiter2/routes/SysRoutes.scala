package dev.rmaiun.arbiter2.routes

import cats.effect.{Concurrent, Sync}
import cats.{Applicative, Monad}
import dev.rmaiun.arbiter2.helpers.OperationalManager
import dev.rmaiun.errorhandling.errors.AppRuntimeException
import dev.rmaiun.errorhandling.errors.codec._
import dev.rmaiun.flowtypes.Flow.Flow
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, Response}
import org.typelevel.log4cats.Logger

object SysRoutes {

  implicit def errorEntityEncoder[F[_]: Applicative, T: Encoder]: EntityEncoder[F, T]      = jsonEncoderOf[F, T]
  implicit def errorEntityDecoder[F[_]: Sync: Concurrent, T: Decoder]: EntityDecoder[F, T] = jsonOf[F, T]

  def flowToResponse[F[_]: Sync: Logger, T](
    flow: Flow[F, T]
  )(implicit ee: EntityEncoder[F, T]): F[Response[F]] = {
    val dsl = new Http4sDsl[F] {}
    import cats.implicits._
    import dsl._
    Monad[F].flatMap(flow.value) {
      case Left(err) =>
        err match {
          case e: AppRuntimeException =>
            for {
              _ <- Logger[F].error(err)("FLow ends with AppRuntimeException")
              x <- Response[F](status = BadRequest).withEntity(ErrorDtoOut(e.code, e.message, e.params)).pure[F]
            } yield x
          case e: Throwable =>
            for {
              _ <- Logger[F].error(err)("FLow ends with Throwable")
              x <- Response[F](status = ServiceUnavailable)
                     .withEntity(ErrorDtoOut("systemException", e.getMessage))
                     .pure[F]
            } yield x
        }
      case Right(value) => Ok(value)
    }
  }

  def sysRoutes[F[_]: Sync: Logger](operMng: OperationalManager[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "ping" =>
        flowToResponse(operMng.ping())
      case GET -> Root / "cache" / "evict" =>
        flowToResponse(operMng.evictCache)
    }
  }

}
