package dev.rmaiun.datamanager.repository

import cats.effect.{ ContextShift, IO }
import dev.rmaiun.datamanager.db.entities._
import dev.rmaiun.datamanager.dtos.internal.{ GameHistoryCriteria, GamePointsCriteria }
import dev.rmaiun.datamanager.helpers.ConfigProvider.Config
import dev.rmaiun.datamanager.helpers.{ ConfigProvider, TransactorProvider }
import dev.rmaiun.datamanager.repositories._
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterEach, OptionValues }

class GameRepoTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach with OptionValues {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  private val config: Config = ConfigProvider.provideConfig
  private val transactor: HikariTransactor[IO] =
    TransactorProvider.hikariTransactor[IO](config, allowPublicKeyRetrieval = true)
  private val algRepo    = AlgorithmRepo.impl[IO]
  private val realmRepo  = RealmRepo.impl[IO]
  private val seasonRepo = SeasonRepo.impl[IO]
  private val userRepo   = UserRepo.impl[IO]
  private val gameRepo   = GameRepo.impl[IO]

  "Game Points" should "create and list data successfully" in {
    val action = for {
      _ <- initTestDataSet()
      _ <- gameRepo.createGamePoint(GamePoints(0, 1, 1, 1, 67))
      _ <- gameRepo.createGamePoint(GamePoints(0, 1, 1, 2, 68))
      _ <- gameRepo.createGamePoint(GamePoints(0, 1, 1, 3, 69))
      _ <- gameRepo.createGamePoint(GamePoints(0, 1, 1, 4, 70))
      _ <- gameRepo.createGamePoint(GamePoints(0, 1, 1, 1, 71))
      _ <- gameRepo.createGamePoint(GamePoints(0, 1, 1, 2, 72))
      _ <- gameRepo.createGamePoint(GamePoints(0, 1, 1, 5, 73))
      _ <- gameRepo.createGamePoint(GamePoints(0, 1, 1, 6, 74))
    } yield ()

    val result = action.transact(transactor).attemptSql.unsafeRunSync()
    result.isRight should be(true)
    val listAction = gameRepo.listPointsByCriteria(GamePointsCriteria("testRealm", Some("S1|2020"), Some("u6")))
    val listData   = listAction.transact(transactor).unsafeRunSync()
    listData.size should be(1)

    val listAction2 = gameRepo.listPointsByCriteria(GamePointsCriteria("testRealm", Some("S1|2020"), Some("u1")))
    val listData2   = listAction2.transact(transactor).unsafeRunSync()
    listData2.size should be(2)
  }

  "Game History" should "create and list data successfully" in {
    val action = for {
      _ <- initTestDataSet()
      _ <- gameRepo.createGameHistory(GameHistory(0, 1, 1, 1, 2, 3, 4))
      _ <- gameRepo.createGameHistory(GameHistory(0, 1, 1, 1, 4, 5, 6, shutout = true))
      _ <- gameRepo.createGameHistory(GameHistory(0, 1, 1, 1, 6, 3, 5))
    } yield ()

    val result = action.transact(transactor).attemptSql.unsafeRunSync()
    result.isRight should be(true)
    val listAction = gameRepo.listHistoryByCriteria(GameHistoryCriteria("testRealm", Some("S1|2020")))
    val listData   = listAction.transact(transactor).unsafeRunSync()
    listData.size should be(3)

    val listAction2 =
      gameRepo.listHistoryByCriteria(GameHistoryCriteria("testRealm", Some("S1|2020")))
    val listData2 = listAction2.transact(transactor).unsafeRunSync()
    listData2.size should be(1)

    val listAction3 =
      gameRepo.listHistoryByCriteria(GameHistoryCriteria("testRealm", Some("S2|2020"), shutout = Some(true)))
    val listData3 = listAction3.transact(transactor).unsafeRunSync()
    listData3.isEmpty should be(true)
  }

  private def initTestDataSet(): ConnectionIO[Unit] =
    for {
      _ <- createTestAlgorithm
      _ <- createTestRealm
      _ <- createTestSeason
      _ <- createTestUsers
    } yield ()

  private def createTestAlgorithm: ConnectionIO[Algorithm] =
    algRepo.create(Algorithm(1, "WinRate"))
  private def createTestRealm: ConnectionIO[Realm] =
    realmRepo.create(Realm(1, "testRealm", 4, 1))
  private def createTestSeason: ConnectionIO[Season] =
    seasonRepo.create(Season(1, "S1|2020", 1, 1))
  private def createTestUsers: ConnectionIO[Unit] =
    for {
      _ <- userRepo.create(User(1, "u1"))
      _ <- userRepo.create(User(2, "u2"))
      _ <- userRepo.create(User(3, "u3"))
      _ <- userRepo.create(User(4, "u4"))
      _ <- userRepo.create(User(5, "u5"))
      _ <- userRepo.create(User(6, "u6"))
    } yield ()

  private def clearRealAlgorithmDBs(): Unit = {
    val action = for {
      _ <- gameRepo.removeNGamePoints()
      _ <- gameRepo.removeNGameHistory()
      _ <- userRepo.removeN()
      _ <- seasonRepo.removeN()
      _ <- realmRepo.removeN()
      _ <- algRepo.removeN()
    } yield ()
    action.transact(transactor).unsafeRunSync()
  }

  override protected def beforeEach(): Unit = clearRealAlgorithmDBs()

  override protected def afterEach(): Unit = clearRealAlgorithmDBs()
}
