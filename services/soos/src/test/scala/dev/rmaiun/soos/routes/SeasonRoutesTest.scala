package dev.rmaiun.soos.routes

import cats.effect.IO
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.protocol.http.SeasonDtoSet.{ NotifySeasonDtoIn, NotifySeasonDtoOut }
import dev.rmaiun.protocol.http.codec.FullCodec._
import dev.rmaiun.soos.db.entities.{ Realm, Season }
import dev.rmaiun.soos.utils.{ Loggable, TestModule }
import io.circe.{ Decoder, Encoder }
import org.http4s.Method.GET
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.ZonedDateTime

case class SeasonRoutesTest() extends AnyFlatSpec with Matchers with BeforeAndAfterEach with Loggable {
  implicit def errorEntityEncoder[T: Encoder]: EntityEncoder[IO, T] = jsonEncoderOf[IO, T]
  implicit def errorEntityDecoder[T: Decoder]: EntityDecoder[IO, T] = jsonOf[IO, T]

  private val seasonManager = TestModule.seasonMng
  private val service       = SoosRoutes.seasonRoutes(seasonManager).orNotFound

  "season/notify" should "successfully handle request" in {
    val response = service.run(
      Request(method = GET, uri = uri"/notify".withQueryParam("season", "S1|1999").withQueryParam("realm", "test2"))
    )
    val data = response.map(r => r.as[NotifySeasonDtoOut]).unsafeRunSync.unsafeRunSync()
    data.season should be("S1|1999")
    data.notified.isBefore(ZonedDateTime.now) should be(true)
  }

  private def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(implicit
    ev: EntityDecoder[IO, A]
  ): Boolean = {
    val actualResp  = actual.unsafeRunSync
    val statusCheck = actualResp.status == expectedStatus
    val bodyCheck = expectedBody.fold[Boolean](actualResp.body.compile.toVector.unsafeRunSync.isEmpty)(expected =>
      actualResp.as[A].unsafeRunSync == expected
    )
    statusCheck && bodyCheck
  }

  private def cleanDb(): Flow[IO, Unit] =
    for {
      _     <- TestModule.realmService.remove(List(99L))
      _     <- TestModule.seasonService.remove()
      realm <- TestModule.realmService.create(Realm(99L, "test2", 4, 1))
      _     <- TestModule.seasonService.storeSeason(Season(100, "S1|1999", 1, realm.id))
    } yield ()

  override protected def beforeEach(): Unit =
    cleanDb().value.unsafeRunSync()

  override protected def afterEach(): Unit =
    cleanDb().value.unsafeRunSync()
}
