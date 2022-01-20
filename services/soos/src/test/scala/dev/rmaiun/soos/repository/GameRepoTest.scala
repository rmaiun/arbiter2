package dev.rmaiun.soos.repository

import cats.data.NonEmptyList
import cats.effect.{ ContextShift, IO }
import cats.syntax.foldable._
import dev.rmaiun.common.DateFormatter
import dev.rmaiun.soos.db.PageInfo
import dev.rmaiun.soos.db.entities._
import dev.rmaiun.soos.dtos.{ EloPointsCriteria, GameHistoryCriteria }
import dev.rmaiun.soos.helpers.ConfigProvider.Config
import dev.rmaiun.soos.helpers.{ ConfigProvider, TransactorProvider }
import dev.rmaiun.soos.repositories._
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterEach, OptionValues }

class GameRepoTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach with OptionValues {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  private val config: Config = ConfigProvider.provideConfig()
  private val transactor: HikariTransactor[IO] =
    TransactorProvider.hikariTransactor[IO](config, allowPublicKeyRetrieval = true)
  private val algRepo    = AlgorithmRepo.impl[IO]
  private val realmRepo  = RealmRepo.impl[IO]
  private val seasonRepo = SeasonRepo.impl[IO]
  private val userRepo   = UserRepo.impl[IO]
  private val gameRepo   = GameRepo.impl[IO]

  "GameRepo" should "create and list Game Points successfully" in {
    val action = for {
      _ <- initTestDataSet()
      _ <- gameRepo.createEloPoint(EloPoints(0, 101, 67, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 102, 68, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 103, 69, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 104, 70, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 101, 71, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 102, 72, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 105, 73, DateFormatter.now))
      _ <- gameRepo.createEloPoint(EloPoints(0, 106, 74, DateFormatter.now))
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

  it should "create and list Game History successfully" in {
    val action = for {
      _ <- initTestDataSet()
      _ <- gameRepo.createGameHistory(GameHistory(0, 100, 100, 101, 102, 103, 104))
      _ <- gameRepo.createGameHistory(GameHistory(0, 100, 100, 101, 104, 105, 106, shutout = true))
      _ <- gameRepo.createGameHistory(GameHistory(0, 100, 100, 101, 106, 103, 105))
    } yield ()

    val result = action.transact(transactor).attemptSql.unsafeRunSync()
    result.isRight should be(true)
    val listAction = gameRepo.listHistoryByCriteria(GameHistoryCriteria("testRealm", Some("S1|2015")))
    val listData   = listAction.transact(transactor).unsafeRunSync()
    listData.size should be(3)

    val listAction2 =
      gameRepo.listHistoryByCriteria(GameHistoryCriteria("testRealm", Some("S1|2015")))
    val listData2 = listAction2.transact(transactor).unsafeRunSync()
    listData2.size should be(3)

    val listAction3 =
      gameRepo.listHistoryByCriteria(GameHistoryCriteria("testRealm", Some("S2|2020"), shutout = Some(true)))
    val listData3 = listAction3.transact(transactor).unsafeRunSync()
    listData3.isEmpty should be(true)
  }

  it should "list elo points with custom pagination" in {
    val initData = (1 to 104)
      .map(id => EloPoints(id, 101, 10, DateFormatter.now))
      .map(ep => gameRepo.createEloPoint(ep))
      .toList
      .sequence_

    val testAction = for {
      _   <- initData
      dto <- gameRepo.listEloPoints(PageInfo(4, 10))
    } yield {
      dto.items.size should be(10)
      dto.items.map(_.id).count(id => id > 40 && id <= 50) should be(10)
      dto.pageResult.total should be(104)
    }
    val result = testAction.transact(transactor).attemptSql.unsafeRunSync()
    result.isRight should be(true)
  }

  it should "list elo points with default pagination" in {
    val initData = (1 to 104)
      .map(id => EloPoints(id, 101, 10, DateFormatter.now))
      .map(ep => gameRepo.createEloPoint(ep))
      .toList
      .sequence_

    val testAction = for {
      _   <- initData
      dto <- gameRepo.listEloPoints(PageInfo())
    } yield {
      dto.items.size should be(104)
      dto.pageResult.total should be(104)
    }
    val result = testAction.transact(transactor).attemptSql.unsafeRunSync()
    result.isRight should be(true)
  }

  it should "list elo points with pagination zero page" in {
    val initData = (1 to 104)
      .map(id => EloPoints(id, 101, 10, DateFormatter.now))
      .map(ep => gameRepo.createEloPoint(ep))
      .toList
      .sequence_

    val testAction = for {
      _   <- initData
      dto <- gameRepo.listEloPoints(PageInfo(0, 5))
    } yield {
      dto.items.size should be(5)
      dto.items.map(_.id).count(id => id < 6) should be(5)
      dto.pageResult.total should be(104)
    }
    val result = testAction.transact(transactor).attemptSql.unsafeRunSync()
    result.isRight should be(true)
  }

  it should "list elo points with pagination last page" in {
    val initData = (1 to 104)
      .map(id => EloPoints(id, 101, 10, DateFormatter.now))
      .map(ep => gameRepo.createEloPoint(ep))
      .toList
      .sequence_

    val testAction = for {
      _   <- initData
      dto <- gameRepo.listEloPoints(PageInfo(10, 10))
    } yield {
      dto.items.size should be(4)
      dto.items.map(_.id).count(id => id > 100) should be(4)
      dto.pageResult.total should be(104)
    }
    val result = testAction.transact(transactor).attemptSql.unsafeRunSync()
    result.isRight should be(true)
  }

  private def initTestDataSet(): ConnectionIO[Unit] =
    for {
      _ <- createTestAlgorithm
      _ <- createTestRealm
      _ <- createTestSeason
      _ <- createTestUsers
    } yield ()

  private def createTestAlgorithm: ConnectionIO[Algorithm] =
    algRepo.create(Algorithm(100, "TestAlgo"))
  private def createTestRealm: ConnectionIO[Realm] =
    realmRepo.create(Realm(100, "testRealm", 4, 100))
  private def createTestSeason: ConnectionIO[Season] =
    seasonRepo.create(Season(100, "S1|2015", 1, 100))
  private def createTestUsers: ConnectionIO[Unit] =
    for {
      _ <- userRepo.create(User(101, "u1"))
      _ <- userRepo.create(User(102, "u2"))
      _ <- userRepo.create(User(103, "u3"))
      _ <- userRepo.create(User(104, "u4"))
      _ <- userRepo.create(User(105, "u5"))
      _ <- userRepo.create(User(106, "u6"))
    } yield ()

  private def clearRealAlgorithmDBs(): Unit = {
    val action = for {
      _ <- gameRepo.removeNEloPoints()
      _ <- gameRepo.removeNGameHistory()
      _ <- userRepo.removeN(List(101, 102, 103, 104, 105, 106))
      _ <- seasonRepo.removeN(List(100))
      _ <- realmRepo.removeN(List(100))
      _ <- algRepo.removeN(List(100))
    } yield ()
    action.transact(transactor).unsafeRunSync()
  }

  override protected def beforeEach(): Unit = clearRealAlgorithmDBs()

  override protected def afterEach(): Unit = clearRealAlgorithmDBs()
}
