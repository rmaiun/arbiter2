package dev.rmaiun.datamanager.repository

import cats.effect.{ ContextShift, IO }
import dev.rmaiun.datamanager.db.entities.{ Algorithm, Season }
import dev.rmaiun.datamanager.helpers.ConfigProvider.Config
import dev.rmaiun.datamanager.helpers.{ ConfigProvider, TransactorProvider }
import dev.rmaiun.datamanager.repositories.{ AlgorithmRepo, SeasonRepo }
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterEach, OptionValues }

import java.time.ZonedDateTime

class SeasonRepoTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach with OptionValues {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  private val config: Config = ConfigProvider.provideConfig
  private val transactor: HikariTransactor[IO] =
    TransactorProvider.hikariTransactor[IO](config, allowPublicKeyRetrieval = true)
  private val seasonRepo: SeasonRepo[IO] = SeasonRepo.impl[IO]
  private val algRepo: AlgorithmRepo[IO] = AlgorithmRepo.impl[IO]
  private val season                     = Season(0, "S1|2020", 1)

  "SeasonRepo" should "insert new season into db and successfully get it" in {
    val action = for {
      alg     <- createTestAlgorithm
      created <- createSeason(season)
      found   <- findSeason(created.id)
    } yield found.map(f => (f, alg))

    val result = action.transact(transactor).unsafeRunSync()
    val data   = result.getOrElse(fail("either was not Right!"))
    data._1 should not be 0
    data._1.name should be(season.name)
    data._2.id should not be 0
  }

  it should "should successfully update Season" in {
    val now = ZonedDateTime.now()
    createTestAlgorithm.transact(transactor).attemptSql.unsafeRunSync()
    val createdSeason = seasonRepo.create(season).transact(transactor).unsafeRunSync()
    val updSeason = seasonRepo
      .update(createdSeason.copy(endNotification = Some(now)))
      .transact(transactor)
      .unsafeRunSync()
    updSeason.endNotification.value should be(now)
  }
  it should "successfully delete 1 of 3 Realms" in {
    val action = for {
      _  <- createTestAlgorithm
      r  <- seasonRepo.create(season)
      r2 <- seasonRepo.create(season.copy(name = "S2|2020"))
      r3 <- seasonRepo.create(season.copy(name = "S3|2020"))
    } yield (r, r2, r3)
    val realmsCreated = action.transact(transactor).unsafeRunSync()

    List(realmsCreated._1.id, realmsCreated._2.id, realmsCreated._3.id) should not contain 0
    val listAll = seasonRepo.listAll.transact(transactor).unsafeRunSync()
    listAll.size should be(3)
    val delete2Elems = seasonRepo
      .removeN(List(realmsCreated._2.id, realmsCreated._3.id))
      .transact(transactor)
      .unsafeRunSync()
    delete2Elems shouldEqual 2
    val listAll2 = seasonRepo.listAll.transact(transactor).unsafeRunSync()
    listAll2.size should be(1)
    listAll2.head.name shouldEqual season.name
  }

  private def createTestAlgorithm: ConnectionIO[Algorithm] =
    algRepo.create(Algorithm(1, "WinRate"))
  private def createSeason(season: Season): ConnectionIO[Season] =
    seasonRepo.create(season)
  private def findSeason(id: Long): ConnectionIO[Option[Season]] =
    seasonRepo.getById(id)

  private def clearRealAlgorithmDBs(): Unit = {
    val action = for {
      _ <- seasonRepo.removeN()
      _ <- algRepo.removeN()
    } yield ()
    action.transact(transactor).attemptSql.unsafeRunSync()
  }

  override protected def beforeEach(): Unit = clearRealAlgorithmDBs()

  override protected def afterEach(): Unit = clearRealAlgorithmDBs()
}
