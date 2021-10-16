package dev.rmaiun.mabel.services

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.protocol.http.GameDtoSet._
import dev.rmaiun.protocol.http.RealmDtoSet._
import dev.rmaiun.protocol.http.UserDtoSet._
import dev.rmaiun.protocol.http.codec.FullCodec._
import io.chrisdavenport.log4cats.Logger
import io.circe.{ Decoder, Encoder }
import org.http4s.Method.POST
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.{ EntityDecoder, EntityEncoder, Request }

case class ArbiterClient[F[_]: Sync: Monad](client: Client[F]) {
  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
  val baseUri                                                    = uri"http://localhost:9091"

  def findRealm(realm: String): Flow[F, GetRealmDtoOut] = {
    val uri = baseUri / "realms" / "find" / s"$realm"
    Flow.effect(client.expect[GetRealmDtoOut](uri))
  }

  def addPlayer(dtoIn: RegisterUserDtoIn): Flow[F, RegisterUserDtoOut] = {
    val uri     = baseUri / "users" / "register"
    val request = Request[F](POST, uri).withEntity(dtoIn)
    Flow.effect(client.expect[RegisterUserDtoOut](request))
  }

  def assignUserToRealm(dtoIn: AssignUserToRealmDtoIn): Flow[F, AssignUserToRealmDtoOut] = {
    val uri     = baseUri / "users" / "assignToRealm"
    val request = Request[F](POST, uri).withEntity(dtoIn)
    Flow.effect(client.expect[AssignUserToRealmDtoOut](request))
  }

  def storeGameHistory(dtoIn: AddGameHistoryDtoIn): Flow[F, AddGameHistoryDtoOut] = {
    val uri     = baseUri / "games" / "history" / "store"
    val request = Request[F](POST, uri).withEntity(dtoIn)
    Flow.effect(client.expect[AddGameHistoryDtoOut](request))
  }

  def storeEloPoints(dtoIn: AddEloPointsDtoIn): Flow[F, AddEloPointsDtoOut] = {
    val uri     = baseUri / "games" / "eloPoints" / "store"
    val request = Request[F](POST, uri).withEntity(dtoIn)
    Flow.effect(client.expect[AddEloPointsDtoOut](request))
  }

  def listGameHistory(realm: String, season: String): Flow[F, ListGameHistoryDtoOut] = {
    val uri = baseUri / "games" / "history" / "list"
    val uriWithParams = uri
      .withQueryParam(realm, s"$realm")
      .withQueryParam("season", s"$season")
    Flow.effect(client.expect[ListGameHistoryDtoOut](uriWithParams))
  }

  def listCalculatedEloPoints(users: List[String]): Flow[F, ListEloPointsDtoOut] = {
    val uri = baseUri / "games" / "eloPoints" / "listCalculated"
    val uriWithParams = uri
      .withQueryParam("users", users.mkString(","))
    Flow.effect(client.expect[ListEloPointsDtoOut](uriWithParams))
  }

  def findPlayer(surname: String): Flow[F, FindUserDtoOut] = {
    val uri           = baseUri / "users" / "find"
    val uriWithParams = uri.withQueryParam("surname", s"$surname")
    Flow.effect(client.expect[FindUserDtoOut](uriWithParams))
  }
}

object ArbiterClient {
  def apply[F[_]](implicit ev: ArbiterClient[F]): ArbiterClient[F] = ev
  def impl[F[_]: Monad: Logger: Sync](c: Client[F]): ArbiterClient[F] =
    new ArbiterClient[F](c)
}
