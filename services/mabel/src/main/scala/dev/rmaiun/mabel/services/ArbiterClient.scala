package dev.rmaiun.mabel.services

import cats.data.EitherT
import cats.effect.Async
import cats.{Functor, Monad}
import dev.rmaiun.errorhandling.errors.codec.ErrorDtoOut
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.errors.Errors.ArbiterClientError
import dev.rmaiun.mabel.services.ConfigProvider.Config
import dev.rmaiun.mabel.utils.Constants
import dev.rmaiun.protocol.http.GameDtoSet._
import dev.rmaiun.protocol.http.RealmDtoSet._
import dev.rmaiun.protocol.http.SeasonDtoSet.{FindSeasonWithoutNotificationDtoOut, NotifySeasonDtoOut}
import dev.rmaiun.protocol.http.UserDtoSet._
import dev.rmaiun.protocol.http.codec.FullCodec._
import dev.rmaiun.serverauth.middleware.Arbiter2Middleware
import io.circe.{Decoder, Encoder}
import org.http4s.Method.{GET, POST}
import org.http4s.Status.BadRequest
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client

case class ArbiterClient[F[_]: Async](client: Client[F])(implicit cfg: Config) {
  import ArbiterClient._
  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
  private val baseUri = Uri
    .fromString(cfg.integration.soos.path)
    .getOrElse(Uri.unsafeFromString(cfg.integration.soos.stub))
  val onError: Response[F] => F[Throwable] = resp => {
    import io.circe.parser._
    resp.status match {
      case BadRequest =>
        val res: EitherT[F, Throwable, ArbiterClientError] = for {
          body     <- Flow.effect(resp.bodyText.compile.string)
          errorDto <- Flow.fromEither(decode[ErrorDtoOut](body))
          error     = ArbiterClientError(errorDto.message)
        } yield error
        Functor[F].map(res.value)(_.fold(err => ArbiterClientError(err.getMessage), ok => ok))
      case _ =>
        Monad[F].pure(ArbiterClientError("Unexpected error occurred"))
    }
  }

  def findRealm(realm: String): Flow[F, GetRealmDtoOut] = {
    val uri = baseUri / "realms" / "find" / s"$realm"
    Flow.effect(client.expectOr[GetRealmDtoOut](uri)(onError))
  }

  def addPlayer(dtoIn: RegisterUserDtoIn): Flow[F, RegisterUserDtoOut] = {
    val uri = baseUri / "users" / "register"
    val request = Request[F](POST, uri)
      .withEntity(dtoIn)
      .withSoosHeaders()
    Flow.effect(client.expectOr[RegisterUserDtoOut](request)(onError))
  }

  def assignUserToRealm(dtoIn: AssignUserToRealmDtoIn): Flow[F, AssignUserToRealmDtoOut] = {
    val uri     = baseUri / "users" / "assignToRealm"
    val request = Request[F](POST, uri).withEntity(dtoIn).withSoosHeaders()

    Flow.effect(client.expectOr[AssignUserToRealmDtoOut](request)(onError))
  }

  def storeGameHistory(dtoIn: AddGameHistoryDtoIn): Flow[F, AddGameHistoryDtoOut] = {
    val uri     = baseUri / "games" / "history" / "store"
    val request = Request[F](POST, uri).withEntity(dtoIn).withSoosHeaders()
    Flow.effect(client.expectOr[AddGameHistoryDtoOut](request)(onError))
  }

  def storeEloPoints(dtoIn: AddEloPointsDtoIn): Flow[F, AddEloPointsDtoOut] = {
    val uri     = baseUri / "games" / "eloPoints" / "store"
    val request = Request[F](POST, uri).withEntity(dtoIn).withSoosHeaders()
    Flow.effect(client.expectOr[AddEloPointsDtoOut](request)(onError))
  }

  def listGameHistory(realm: String, season: String): Flow[F, ListGameHistoryDtoOut] = {
    val uri = baseUri / "games" / "history" / "list"
    val uriWithParams = uri
      .withQueryParam("realm", s"$realm")
      .withQueryParam("season", s"$season")
    val request = Request[F](GET, uriWithParams).withSoosHeaders()
    Flow.effect(client.expectOr[ListGameHistoryDtoOut](request)(onError))
  }

  def listCalculatedEloPoints(users: List[String]): Flow[F, ListEloPointsDtoOut] = {
    val uri = baseUri / "games" / "eloPoints" / "listCalculated"
    val uriWithParams = uri
      .withQueryParam("users", users.mkString(","))
    val request = Request[F](GET, uriWithParams).withSoosHeaders()
    Flow.effect(client.expectOr[ListEloPointsDtoOut](request)(onError))
  }

  def findPlayerBySurname(surname: String): Flow[F, FindUserDtoOut] = {
    val uri           = baseUri / "users" / "find"
    val uriWithParams = uri.withQueryParam("surname", s"$surname")
    val request       = Request[F](GET, uriWithParams).withSoosHeaders()
    Flow.effect(client.expectOr[FindUserDtoOut](request)(onError))
  }

  def findPlayerByTid(tid: Long): Flow[F, FindUserDtoOut] = {
    val uri           = baseUri / "users" / "find"
    val uriWithParams = uri.withQueryParam("tid", s"$tid")
    val request       = Request[F](GET, uriWithParams).withSoosHeaders()
    Flow.effect(client.expectOr[FindUserDtoOut](request)(onError))
  }

  def findAllPlayers: Flow[F, FindAllUsersDtoOut] = {
    val uri           = baseUri / "users" / "list"
    val uriWithParams = uri.withQueryParam("realm", s"${Constants.defaultRealm}").withQueryParam("activeStatus", true)
    val request       = Request[F](GET, uriWithParams).withSoosHeaders()
    Flow.effect(client.expectOr[FindAllUsersDtoOut](request)(onError))
  }

  def findRealmAdmins(realm: String = Constants.defaultRealm): Flow[F, FindRealmAdminsDtoOut] = {
    val uri           = baseUri / "users" / "listAdminsForRealm"
    val uriWithParams = uri.withQueryParam("realm", s"$realm")
    val request       = Request[F](GET, uriWithParams).withSoosHeaders()
    Flow.effect(client.expectOr[FindRealmAdminsDtoOut](request)(onError))
  }
  def findSeasonWithoutNotifications: Flow[F, FindSeasonWithoutNotificationDtoOut] = {
    val uri           = baseUri / "seasons" / "findWithoutNotification"
    val uriWithParams = uri.withQueryParam("realm", s"${Constants.defaultRealm}")
    val request       = Request[F](GET, uriWithParams).withSoosHeaders()
    Flow.effect(client.expectOr[FindSeasonWithoutNotificationDtoOut](request)(onError))
  }

  def notifySeason(season: String): Flow[F, NotifySeasonDtoOut] = {
    val uri = baseUri / "seasons" / "notify"
    val uriWithParams = uri
      .withQueryParam("realm", s"${Constants.defaultRealm}")
      .withQueryParam("season", season)
    val request = Request[F](GET, uriWithParams).withSoosHeaders()
    Flow.effect(client.expectOr[NotifySeasonDtoOut](request)(onError))
  }
}

object ArbiterClient {
  def apply[F[_]](implicit ev: ArbiterClient[F]): ArbiterClient[F] = ev
  def impl[F[_]: Async](c: Client[F])(implicit cfg: Config): ArbiterClient[F] =
    new ArbiterClient[F](c)

  implicit class RichRequest[F[_]](r: Request[F]) {
    def withSoosHeaders()(implicit cfg: Config): Request[F] =
      r.withHeaders(Header.Raw(Arbiter2Middleware.Arbiter2AuthHeader, cfg.integration.soos.token))
  }
}
