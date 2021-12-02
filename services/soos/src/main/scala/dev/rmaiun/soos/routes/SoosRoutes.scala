package dev.rmaiun.soos.routes

import cats.effect.Sync
import cats.{Applicative, Monad}
import dev.rmaiun.errorhandling.errors.AppRuntimeException
import dev.rmaiun.errorhandling.errors.codec._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.protocol.http.GameDtoSet.{AddEloPointsDtoIn, AddGameHistoryDtoIn, ListEloPointsDtoIn, ListGameHistoryDtoIn}
import dev.rmaiun.protocol.http.RealmDtoSet.{GetRealmDtoIn, RegisterRealmDtoIn, UpdateRealmAlgorithmDtoIn}
import dev.rmaiun.protocol.http.SeasonDtoSet.{CreateSeasonDtoIn, FindSeasonWithoutNotificationDtoIn, NotifySeasonDtoIn}
import dev.rmaiun.protocol.http.UserDtoSet._
import dev.rmaiun.soos.errors.RoutingErrors.RequiredParamsNotFound
import dev.rmaiun.soos.managers.{GameManager, RealmManager, SeasonManager, UserManager}
import io.chrisdavenport.log4cats.Logger
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, Response}
object SoosRoutes {

  implicit def errorEntityEncoder[F[_]: Applicative, T: Encoder]: EntityEncoder[F, T] = jsonEncoderOf[F, T]
  implicit def errorEntityDecoder[F[_]: Sync, T: Decoder]: EntityDecoder[F, T]        = jsonOf[F, T]

  def flowToResponse[F[_]: Sync: Monad: Logger, T](
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

  def realmRoutes[F[_]: Sync: Monad: Logger](realmManager: RealmManager[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dev.rmaiun.protocol.http.codec.FullCodec._
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root / "find" / name =>
        flowToResponse(realmManager.findRealm(GetRealmDtoIn(name)))

      case req @ POST -> Root / "create" =>
        val dtoOut = for {
          dtoIn <- Flow.effect(req.as[RegisterRealmDtoIn])
          res   <- realmManager.registerRealm(dtoIn)
        } yield res
        flowToResponse(dtoOut)

      case req @ POST -> Root / "updateAlgorithm" =>
        val dtoOut = for {
          dtoIn <- Flow.effect(req.as[UpdateRealmAlgorithmDtoIn])
          res   <- realmManager.updateRealmAlgorithm(dtoIn)
        } yield res
        flowToResponse(dtoOut)
    }
  }

  def userRoutes[F[_]: Sync: Monad: Logger](userManager: UserManager[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dev.rmaiun.protocol.http.codec.FullCodec._
    import dsl._

    HttpRoutes.of[F] {
      case req @ POST -> Root / "register" =>
        val dtoOut = for {
          dtoIn <- Flow.effect(req.as[RegisterUserDtoIn])
          res   <- userManager.registerUser(dtoIn)
        } yield res
        flowToResponse(dtoOut)

      case req @ GET -> Root / "find" =>
        val surname = req.params.get("surname")
        val tid     = req.params.get("tid").map(_.toLong)
        flowToResponse(userManager.findUser(FindUserDtoIn(surname, tid)))

      case req @ GET -> Root / "list" =>
        val actStatus = req.params.get("activeStatus").map(_.toBoolean)
        val userAllFlow = for {
          realm  <- Flow.fromOpt(req.params.get("realm"), RequiredParamsNotFound(Map("requestParam" -> "realm")))
          result <- userManager.findAllUsers(FindAllUsersDtoIn(realm, actStatus))
        } yield result
        flowToResponse(userAllFlow)

      case req @ POST -> Root / "assignToRealm" =>
        val dtoOut = for {
          dtoIn <- Flow.effect(req.as[AssignUserToRealmDtoIn])
          res   <- userManager.assignUserToRealm(dtoIn)
        } yield res
        flowToResponse(dtoOut)

      case req @ POST -> Root / "switchActiveRealm" =>
        val dtoOut = for {
          dtoIn <- Flow.effect(req.as[SwitchActiveRealmDtoIn])
          res   <- userManager.switchActiveRealm(dtoIn)
        } yield res
        flowToResponse(dtoOut)

      case req @ POST -> Root / "processActivation" =>
        val dtoOut = for {
          dtoIn <- Flow.effect(req.as[ProcessActivationDtoIn])
          res   <- userManager.processActivation(dtoIn)
        } yield res
        flowToResponse(dtoOut)

      case req @ POST -> Root / "linkTid" =>
        val dtoOut = for {
          dtoIn <- Flow.effect(req.as[LinkTidDtoIn])
          res   <- userManager.linkTid(dtoIn)
        } yield res
        flowToResponse(dtoOut)

      case req @ POST -> Root / "availableRealms" =>
        val dtoOut = for {
          dtoIn <- Flow.effect(req.as[FindAvailableRealmsDtoIn])
          res   <- userManager.findAvailableRealms(dtoIn)
        } yield res
        flowToResponse(dtoOut)

      case req @ GET -> Root / "listAdminsForRealm" =>
        val userAllFlow = for {
          realm  <- Flow.fromOpt(req.params.get("realm"), RequiredParamsNotFound(Map("requestParam" -> "realm")))
          result <- userManager.findRealmAdmins(FindRealmAdminsDtoIn(realm))
        } yield result
        flowToResponse(userAllFlow)
    }
  }

  def gameHistoryRoutes[F[_]: Sync: Monad: Logger](gameManager: GameManager[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dev.rmaiun.protocol.http.codec.FullCodec._
    import dsl._

    HttpRoutes.of[F] {
      case req @ POST -> Root / "store" =>
        val dtoOut = for {
          dtoIn <- Flow.effect(req.as[AddGameHistoryDtoIn])
          res   <- gameManager.storeGameHistory(dtoIn)
        } yield res
        flowToResponse(dtoOut)

      case req @ GET -> Root / "list" =>
        val userAllFlow = for {
          realm  <- Flow.fromOpt(req.params.get("realm"), RequiredParamsNotFound(Map("requestParam" -> "realm")))
          season <- Flow.fromOpt(req.params.get("season"), RequiredParamsNotFound(Map("requestParam" -> "season")))
          result <- gameManager.listGameHistory(ListGameHistoryDtoIn(realm, season))
        } yield result
        flowToResponse(userAllFlow)
    }
  }

  def eloPointsRoutes[F[_]: Sync: Monad: Logger](gameManager: GameManager[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dev.rmaiun.protocol.http.codec.FullCodec._
    import dsl._

    HttpRoutes.of[F] {

      case req @ POST -> Root / "store" =>
        val dtoOut = for {
          dtoIn <- Flow.effect(req.as[AddEloPointsDtoIn])
          res   <- gameManager.addEloPoints(dtoIn)
        } yield res
        flowToResponse(dtoOut)

      case req @ GET -> Root / "listCalculated" =>
        val userAllFlow = for {
          users    <- Flow.fromOpt(req.params.get("users"), RequiredParamsNotFound(Map("requestParam" -> "users")))
          listUsers = users.split(",").toList
          result   <- gameManager.listCalculatedEloPoints(ListEloPointsDtoIn(Some(listUsers)))
        } yield result
        flowToResponse(userAllFlow)
    }
  }

  def seasonRoutes[F[_]: Sync: Monad: Logger](seasonManager: SeasonManager[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dev.rmaiun.protocol.http.codec.FullCodec._
    import dsl._

    HttpRoutes.of[F] {

      case req @ POST -> Root / "create" =>
        val dtoOut = for {
          dtoIn <- Flow.effect(req.as[CreateSeasonDtoIn])
          res   <- seasonManager.createSeason(dtoIn)
        } yield res
        flowToResponse(dtoOut)

      case req @ GET -> Root / "findWithoutNotification" =>
        val dtoOut = for {
          realm  <- Flow.fromOpt(req.params.get("realm"), RequiredParamsNotFound(Map("requestParam" -> "realm")))
          result <- seasonManager.findSeasonWithoutNotification(FindSeasonWithoutNotificationDtoIn(realm))
        } yield result
        flowToResponse(dtoOut)

      case req @ GET -> Root / "notify" =>
        val dtoOut = for {
          season <- Flow.fromOpt(req.params.get("season"), RequiredParamsNotFound(Map("requestParam" -> "season")))
          realm  <- Flow.fromOpt(req.params.get("realm"), RequiredParamsNotFound(Map("requestParam" -> "realm")))
          result <- seasonManager.notifySeason(NotifySeasonDtoIn(season, realm))
        } yield result
        flowToResponse(dtoOut)
    }
  }
}
