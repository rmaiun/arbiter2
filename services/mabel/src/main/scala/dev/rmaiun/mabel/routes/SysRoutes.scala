package dev.rmaiun.mabel.routes

import cats.effect.{Async, Concurrent, Sync}
import cats.{Applicative, Monad}
import dev.rmaiun.errorhandling.errors.AppRuntimeException
import dev.rmaiun.errorhandling.errors.codec._
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.Program.InternalCache
import dev.rmaiun.mabel.services.PingManager
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, Response}
import org.typelevel.log4cats.Logger
import sttp.model.StatusCode

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
            val app = if (e.app.isEmpty) { Some("datamanager") }
            else e.app
            for {
              _ <- Logger[F].error(err)("FLow ends with AppRuntimeException")
              x <- Response[F](status = BadRequest).withEntity(ErrorDtoOut(e.code, e.message, app, e.params)).pure[F]
            } yield x
          case e: Throwable =>
            for {
              _ <- Logger[F].error(err)("FLow ends with Throwable")
              x <- Response[F](status = ServiceUnavailable)
                     .withEntity(ErrorDtoOut("systemException", e.getMessage, Some("datamanager")))
                     .pure[F]
            } yield x
        }
      case Right(value) => Ok(value)
    }
  }

  case class MetricCriteria(value: String)
  implicit val MetricCriteriaEncoder: Encoder[MetricCriteria] = deriveEncoder[MetricCriteria]
  implicit val MetricCriteriaDecoder: Decoder[MetricCriteria] = deriveDecoder[MetricCriteria]

  case class MetricResult(cache: String, data: Map[String, String])
  implicit val MetricResultEncoder: Encoder[MetricResult]   = deriveEncoder[MetricResult]
  implicit val MetricResultInDecoder: Decoder[MetricResult] = deriveDecoder[MetricResult]

  def sysRoutes[F[_]: Async: Logger](pingMng: PingManager[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] { case GET -> Root / "ping" =>
      flowToResponse(pingMng.ping())
    }
  }

  def cacheMetrics[F[_]: Async](cache: InternalCache): HttpRoutes[F] = {
    import sttp.tapir._
    import sttp.tapir.docs.openapi._
    import sttp.tapir.generic.auto._
    import sttp.tapir.json.circe._
    import sttp.tapir.openapi.circe.yaml._
    import sttp.tapir.server.http4s._

    val baseEndpoint = endpoint
      .in("api" / "v1")
      .errorOut(statusCode.and(jsonBody[ErrorDtoOut]))

    val body = jsonBody[MetricCriteria]
      .description("Metric input dto description, where value is oneOf['cache']")
      .example(MetricCriteria("cache"))

    val cacheMetricsEndpoint: Endpoint[Unit, MetricCriteria, (StatusCode, ErrorDtoOut), MetricResult, Any] =
      baseEndpoint.get
        .in("metrics")
        .in(body)
        .out(jsonBody[MetricResult])

    val docs = OpenAPIDocsInterpreter().toOpenAPI(cacheMetricsEndpoint, "Mabel", "1.0")
    println(docs.toYaml)
    Http4sServerInterpreter().toRoutes(cacheMetricsEndpoint.serverLogic(dtoIn => cacheStats(dtoIn, cache)))
  }

  private def cacheStats[F[_]](dtoIn: MetricCriteria, cache: InternalCache)(implicit
    F: Sync[F]
  ): F[Either[(StatusCode, ErrorDtoOut), MetricResult]] =
    dtoIn.value match {
      case "cache" => F.pure(Right(MetricResult("test", cache.asMap().toMap)))
      case _       => F.pure(Left((StatusCode.BadRequest, ErrorDtoOut("invalidCriteria", "Invalid criteria", Some("mabel")))))
    }

}
