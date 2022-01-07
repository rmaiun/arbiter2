package dev.rmaiun.soos.repository

import cats.effect.{ ContextShift, IO }
import dev.rmaiun.soos.db.entities.{ Algorithm, Realm, Season }
import dev.rmaiun.soos.helpers.ConfigProvider.Config
import dev.rmaiun.soos.helpers.{ ConfigProvider, TransactorProvider }
import dev.rmaiun.soos.repositories.{ AlgorithmRepo, RealmRepo, SeasonRepo }
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
  private val realmRepo: RealmRepo[IO]   = RealmRepo.impl[IO]
  private val algRepo: AlgorithmRepo[IO] = AlgorithmRepo.impl[IO]

  private val defaultRealm  = Realm(88, "realm1", 2, 1)
  private val defaultSeason = Season(99, "S1|2015", 1, defaultRealm.id)

  "SeasonRepo" should "insert new season into db and successfully get it" in {
    val action = for {
      _       <- createTestRealm
      created <- createSeason(defaultSeason)
      found   <- findSeason(created.id)
    } yield found

    val result = action.transact(transactor).unsafeRunSync()
    val data   = result.getOrElse(fail("either was not Right!"))
    data should not be 0
    data.name should be(defaultSeason.name)
  }

  it should "successfully get by season name and realm name" in {
    val action = for {
      alg     <- createTestAlgorithm
      _       <- createTestRealm
      created <- createSeason(defaultSeason)
      found   <- seasonRepo.getBySeasonNameRealm(defaultSeason.name, defaultRealm.name)
    } yield found

    val result = action.transact(transactor).unsafeRunSync()
    val data   = result.getOrElse(fail("either was not Right!"))
    data should not be 0
    data.name should be(defaultSeason.name)
  }

  it should "should successfully update Season" in {
    val now = ZonedDateTime.now()
    createTestRealm.transact(transactor).attemptSql.unsafeRunSync()
    val createdSeason = seasonRepo.create(defaultSeason).transact(transactor).unsafeRunSync()
    val updSeason = seasonRepo
      .update(createdSeason.copy(endNotification = Some(now)))
      .transact(transactor)
      .unsafeRunSync()
    updSeason.endNotification.value should be(now)
  }

  it should "successfully delete 1 of 3 seasons" in {
    val action = for {
      _  <- createTestRealm
      r  <- seasonRepo.create(defaultSeason)
      r2 <- seasonRepo.create(defaultSeason.copy(name = "S3|2015", id = 100))
      r3 <- seasonRepo.create(defaultSeason.copy(name = "S4|2015", id = 101))
    } yield (r, r2, r3)
    val realmsCreated = action.transact(transactor).unsafeRunSync()

    List(realmsCreated._1.id, realmsCreated._2.id, realmsCreated._3.id) should not contain 0
    val listAll = seasonRepo.listAll().transact(transactor).unsafeRunSync()
    listAll.size should be(3)
    val delete2Elems = seasonRepo
      .removeN(List(realmsCreated._2.id, realmsCreated._3.id))
      .transact(transactor)
      .unsafeRunSync()
    delete2Elems shouldEqual 2
    val listAll2 = seasonRepo.listAll().transact(transactor).unsafeRunSync()
    listAll2.size should be(1)
    listAll2.head.name shouldEqual defaultSeason.name
  }

  private def createTestAlgorithm: ConnectionIO[Algorithm] =
    algRepo.create(Algorithm(100, "TestAlgo"))

  private def createTestRealm: ConnectionIO[Realm] =
    realmRepo.create(defaultRealm)

  private def createSeason(season: Season): ConnectionIO[Season] =
    seasonRepo.create(season)

  private def findSeason(id: Long): ConnectionIO[Option[Season]] =
    seasonRepo.getById(id)

  private def clearRealAlgorithmDBs(): Unit = {
    val action = for {
      _ <- seasonRepo.removeN(List(defaultSeason.id))
      _ <- realmRepo.removeN(List(defaultRealm.id))
      _ <- algRepo.removeN(List(100))
    } yield ()
    action.transact(transactor).attemptSql.unsafeRunSync()
  }

  override protected def beforeEach(): Unit = clearRealAlgorithmDBs()

  override protected def afterEach(): Unit = clearRealAlgorithmDBs()
}
