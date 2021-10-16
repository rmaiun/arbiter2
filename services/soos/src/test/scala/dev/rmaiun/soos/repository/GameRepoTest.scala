package dev.rmaiun.soos.repository

import cats.data.NonEmptyList
import cats.effect.{ContextShift, IO}
import dev.rmaiun.common.DateFormatter
import dev.rmaiun.soos.db.entities._
import dev.rmaiun.soos.dtos.{EloPointsCriteria, GameHistoryCriteria}
import dev.rmaiun.soos.helpers.ConfigProvider.Config
import dev.rmaiun.soos.helpers.{ConfigProvider, TransactorProvider}
import dev.rmaiun.soos.repositories._
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}

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
      _ <- gameRepo.createEloPoint(EloPoints(0, 1, 67, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 2, 68, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 3, 69, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 4, 70, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 1, 71, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 2, 72, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 5, 73, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 6, 74, DateFormatter.now))
    } yield ()

    val result = action.transact(transactor).attemptSql.unsafeRunSync()
    result.isRight should be(true)
    val listAction = gameRepo.listEloPointsByCriteria(EloPointsCriteria(NonEmptyList.fromList(List("u6"))))
    val listData   = listAction.transact(transactor).unsafeRunSync()
    listData.size should be(1)

    val listAction2 = gameRepo.listEloPointsByCriteria(EloPointsCriteria(NonEmptyList.fromList(List("u1"))))
    val listData2   = listAction2.transact(transactor).unsafeRunSync()
    listData2.size should be(2)

    val listAction3 = gameRepo.listCalculatedPoints()
    val listData3   = listAction3.transact(transactor).unsafeRunSync()
    listData3.size should be(6)
    listData3.map(_.points) should contain allElementsOf Seq(138, 140, 69, 70, 73, 74)
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
    listData2.size should be(3)

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
    algRepo.create(Algorithm(1, "WinLoss"))
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
      _ <- gameRepo.removeNEloPoints()
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
