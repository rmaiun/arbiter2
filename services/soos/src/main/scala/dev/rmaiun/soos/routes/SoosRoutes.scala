package dev.rmaiun.soos.routes

import cats.effect.{Async, Sync}
import cats.{Applicative, Monad}
import dev.rmaiun.errorhandling.errors.AppRuntimeException
import dev.rmaiun.errorhandling.errors.codec._
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{FLog, Flow}
import dev.rmaiun.protocol.http.GameDtoSet.{AddEloPointsDtoIn, AddGameHistoryDtoIn, ListEloPointsDtoIn, ListGameHistoryDtoIn}
import dev.rmaiun.protocol.http.RealmDtoSet.{GetRealmDtoIn, RegisterRealmDtoIn, UpdateRealmAlgorithmDtoIn}
import dev.rmaiun.protocol.http.SeasonDtoSet.{CreateSeasonDtoIn, FindSeasonWithoutNotificationDtoIn, NotifySeasonDtoIn}
import dev.rmaiun.protocol.http.UserDtoSet._
import dev.rmaiun.soos.managers.{GameManager, RealmManager, SeasonManager, UserManager}
import org.typelevel.log4cats.Logger
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.{OptionalQueryParamDecoderMatcher, QueryParamDecoderMatcher}
object SoosRoutes {

  implicit def errorEntityEncoder[F[_]: Applicative, T: Encoder]: EntityEncoder[F, T] = jsonEncoderOf[F, T]
  implicit def errorEntityDecoder[F[_]: Async, T: Decoder]: EntityDecoder[F, T]        = jsonOf[F, T]
  implicit val dataListQueryParamDecoder: QueryParamDecoder[List[String]] =
    QueryParamDecoder[String].map(_.split(",").toList)

  object RealmQueryParamMatcher           extends QueryParamDecoderMatcher[String]("realm")
  object SeasonQueryParamMatcher          extends QueryParamDecoderMatcher[String]("season")
  object OptActiveStatusQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Boolean]("activeStatus")
  object OptSurnameQueryParamMatcher      extends OptionalQueryParamDecoderMatcher[String]("surname")
  object OptTidQueryParamMatcher          extends OptionalQueryParamDecoderMatcher[Long]("tid")
  object UsersQueryParamMatcher           extends QueryParamDecoderMatcher[List[String]]("users")

  def flowToResponse[F[_]: Async: Logger, T](
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

  def realmRoutes[F[_]: Async: Logger](realmManager: RealmManager[F]): HttpRoutes[F] = {
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

  def userRoutes[F[_]: Async: Logger](userManager: UserManager[F]): HttpRoutes[F] = {
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

      case GET -> Root / "find" :? OptSurnameQueryParamMatcher(surname) +& OptTidQueryParamMatcher(tid) =>
        flowToResponse(userManager.findUser(FindUserDtoIn(surname, tid)))

      case GET -> Root / "list" :? OptActiveStatusQueryParamMatcher(activeStatus) +& RealmQueryParamMatcher(realm) =>
        val userAllFlow = for {
          result <- userManager.findAllUsers(FindAllUsersDtoIn(realm, activeStatus))
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

      case GET -> Root / "listAdminsForRealm" :? RealmQueryParamMatcher(realm) =>
        val userAllFlow = for {
          result <- userManager.findRealmAdmins(FindRealmAdminsDtoIn(realm))
        } yield result
        flowToResponse(userAllFlow)
    }
  }

  def gameHistoryRoutes[F[_]: Async: Logger](gameManager: GameManager[F]): HttpRoutes[F] = {
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

      case GET -> Root / "list" :? RealmQueryParamMatcher(realm) +& SeasonQueryParamMatcher(season) =>
        val userAllFlow = for {
          result <- gameManager.listGameHistory(ListGameHistoryDtoIn(realm, season))
        } yield result
        flowToResponse(userAllFlow)
    }
  }

  def eloPointsRoutes[F[_]: Async: Logger](gameManager: GameManager[F]): HttpRoutes[F] = {
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

      case GET -> Root / "listCalculated" :? UsersQueryParamMatcher(users) =>
        val userAllFlow = for {
          result <- gameManager.listCalculatedEloPoints(ListEloPointsDtoIn(Some(users)))
        } yield result
        flowToResponse(userAllFlow)
    }
  }

  def seasonRoutes[F[_]: Async: Logger](seasonManager: SeasonManager[F]): HttpRoutes[F] = {
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

      case GET -> Root / "findWithoutNotification" :? RealmQueryParamMatcher(realm) =>
        val dtoOut = for {
          result <- seasonManager.findSeasonWithoutNotification(FindSeasonWithoutNotificationDtoIn(realm))
        } yield result
        flowToResponse(dtoOut)

      case GET -> Root / "notify" :? RealmQueryParamMatcher(realm) +& SeasonQueryParamMatcher(season) =>
        val dtoOut = for {
          result <- seasonManager.notifySeason(NotifySeasonDtoIn(season, realm))
        } yield result
        flowToResponse(dtoOut)
    }
  }

  def archiveRoutes[F[_]: Async: Logger]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] { case req @ GET -> Root / "redirect" =>
      val dtoOut = for {
        _ <- FLog.info(s"Received URI: ${req.uri.toString()}")
//        _ <- FLog.info(s"Received Params: $xxx")
//        _ <- FLog.info(s"Received Headers: $bbb")
      } yield "OK"
      flowToResponse(dtoOut)
    }
  }
}
