package dev.rmaiun.serverauth.middleware

import cats.data.{ Kleisli, OptionT }
import cats.{ Applicative, MonadError }
import dev.rmaiun.errorhandling.errors.codec.ErrorDtoOut
import dev.rmaiun.serverauth.errors.AuthError
import io.circe.Encoder
import org.http4s.Status.Forbidden
import org.http4s.circe.jsonEncoderOf
import org.http4s.{ EntityEncoder, HttpRoutes, Request, Response }
import org.typelevel.ci.CIString
object Arbiter2Middleware {
  val Arbiter2AuthHeader: CIString                                                            = CIString("Arbiter2Auth")
  private implicit def errorEntityEncoder[F[_]: Applicative, T: Encoder]: EntityEncoder[F, T] = jsonEncoderOf[F, T]
  def apply[F[_]](service: HttpRoutes[F], allowedTokens: List[String])(implicit
    ME: MonadError[F, Throwable]
  ): HttpRoutes[F] = Kleisli { req: Request[F] =>
    val header = req.headers.get(Arbiter2AuthHeader).toRight(AuthError("Unsuccessful authorization"))
    header.fold(
      err =>
        if (req.uri.path.startsWithString("archive/redirect")) {
          service(req)
        } else {
          OptionT.pure(
            Response[F](status = Forbidden)
              .withEntity(ErrorDtoOut("authException", err.msg, Some("datamanager")))
          )
        },
      header =>
        if (allowedTokens.contains(header.head.value)) {
          service(req)
        } else {
          OptionT.pure(
            Response[F](status = Forbidden)
              .withEntity(ErrorDtoOut("authException", "Authentication failed", Some("datamanager")))
          )
        }
    )
  }
}
