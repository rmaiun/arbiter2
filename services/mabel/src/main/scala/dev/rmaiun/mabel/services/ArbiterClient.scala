package dev.rmaiun.mabel.services

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.errorhandling.errors.codec.ErrorDtoOut
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.errors.Errors.ArbiterClientError
import dev.rmaiun.mabel.services.ConfigProvider.ServerConfig
import dev.rmaiun.mabel.utils.Constants
import dev.rmaiun.protocol.http.GameDtoSet._
import dev.rmaiun.protocol.http.RealmDtoSet._
import dev.rmaiun.protocol.http.UserDtoSet._
import dev.rmaiun.protocol.http.codec.FullCodec._
import io.chrisdavenport.log4cats.Logger
import io.circe.{ Decoder, Encoder }
import org.http4s.Method.POST
import org.http4s.Status.BadRequest
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client

case class ArbiterClient[F[_]: Sync: Monad: Logger](client: Client[F], cfg: ServerConfig) {
  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
  private val baseUri = Uri
    .fromString(cfg.mabelPath)
    .getOrElse(Uri.unsafeFromString(cfg.mabelPathMock))
  val onError: Response[F] => F[Throwable] = resp => {
    import cats.syntax.functor._
    import io.circe.parser._
    resp.status match {
      case BadRequest =>
        val res = for {
          body     <- Flow.effect(resp.bodyText.compile.string)
          errorDto <- Flow.fromEither(decode[ErrorDtoOut](body))
          error     = ArbiterClientError(errorDto.message)
        } yield error
        res.value.map(_.fold(err => ArbiterClientError(err.getMessage), ok => ok))
      case _ =>
        Monad[F].pure(ArbiterClientError("Unexpected error occurred"))
    }
  }

  def findRealm(realm: String): Flow[F, GetRealmDtoOut] = {
    val uri = baseUri / "realms" / "find" / s"$realm"
    Flow.effect(client.expectOr[GetRealmDtoOut](uri)(onError))
  }

  def addPlayer(dtoIn: RegisterUserDtoIn): Flow[F, RegisterUserDtoOut] = {
    val uri     = baseUri / "users" / "register"
    val request = Request[F](POST, uri).withEntity(dtoIn)
    Flow.effect(client.expectOr[RegisterUserDtoOut](request)(onError))
  }

  def assignUserToRealm(dtoIn: AssignUserToRealmDtoIn): Flow[F, AssignUserToRealmDtoOut] = {
    val uri     = baseUri / "users" / "assignToRealm"
    val request = Request[F](POST, uri).withEntity(dtoIn)
    Flow.effect(client.expectOr[AssignUserToRealmDtoOut](request)(onError))
  }

  def storeGameHistory(dtoIn: AddGameHistoryDtoIn): Flow[F, AddGameHistoryDtoOut] = {
    val uri     = baseUri / "games" / "history" / "store"
    val request = Request[F](POST, uri).withEntity(dtoIn)
    Flow.effect(client.expectOr[AddGameHistoryDtoOut](request)(onError))
  }

  def storeEloPoints(dtoIn: AddEloPointsDtoIn): Flow[F, AddEloPointsDtoOut] = {
    val uri     = baseUri / "games" / "eloPoints" / "store"
    val request = Request[F](POST, uri).withEntity(dtoIn)
    Flow.effect(client.expectOr[AddEloPointsDtoOut](request)(onError))
  }

  def listGameHistory(realm: String, season: String): Flow[F, ListGameHistoryDtoOut] = {
    val uri = baseUri / "games" / "history" / "list"
    val uriWithParams = uri
      .withQueryParam("realm", s"$realm")
      .withQueryParam("season", s"$season")
    Flow.effect(client.expectOr[ListGameHistoryDtoOut](uriWithParams)(onError))
  }

  def listCalculatedEloPoints(users: List[String]): Flow[F, ListEloPointsDtoOut] = {
    val uri = baseUri / "games" / "eloPoints" / "listCalculated"
    val uriWithParams = uri
      .withQueryParam("users", users.mkString(","))
    Flow.effect(client.expectOr[ListEloPointsDtoOut](uriWithParams)(onError))
  }

  def findPlayer(surname: String): Flow[F, FindUserDtoOut] = {
    val uri           = baseUri / "users" / "find"
    val uriWithParams = uri.withQueryParam("surname", s"$surname")
    Flow.effect(client.expectOr[FindUserDtoOut](uriWithParams)(onError))
  }

  def findAllPlayers: Flow[F, FindAllUsersDtoOut] = {
    val uri           = baseUri / "users" / "list"
    val uriWithParams = uri.withQueryParam("realm", s"${Constants.defaultRealm}").withQueryParam("activeStatus", true)
    Flow.effect(client.expectOr[FindAllUsersDtoOut](uriWithParams)(onError))
  }
}

object ArbiterClient {
  def apply[F[_]](implicit ev: ArbiterClient[F]): ArbiterClient[F] = ev
  def impl[F[_]: Monad: Logger: Sync](c: Client[F], cfg: ServerConfig): ArbiterClient[F] =
    new ArbiterClient[F](c, cfg)
}
