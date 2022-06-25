package dev.rmaiun.arbiter2.repository

import cats.data.NonEmptyList
import cats.effect.IO
import dev.rmaiun.arbiter2.db.entities.{ Realm, Role, User, UserRealmRole }
import dev.rmaiun.arbiter2.db.entities._
import dev.rmaiun.arbiter2.helpers.ConfigProvider.Config
import dev.rmaiun.arbiter2.helpers.{ ConfigProvider, TransactorProvider }
import dev.rmaiun.arbiter2.repositories.{ RealmRepo, RoleRepo, UserRepo }
import dev.rmaiun.arbiter2.utils.IoTestRuntime
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterEach, OptionValues }

class UserRepoTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach with OptionValues with IoTestRuntime {

  private val config: Config = ConfigProvider.provideConfig
  private val transactor: HikariTransactor[IO] =
    TransactorProvider.hikariTransactor[IO](config, allowPublicKeyRetrieval = true)
  private val realmRepo: RealmRepo[IO] = RealmRepo.impl[IO]
  private val userRepo: UserRepo[IO]   = UserRepo.impl[IO]
  private val roleRepo                 = RoleRepo.impl[IO]
  private val role                     = Role(105, "admin2", 50)
  private val realm1                   = Realm(101, "ua_ky", 4, 1)
  private val realm2                   = Realm(102, "ua_te", 4, 1)

  private val user1 = User(103, "user1", Some("x1"))
  private val user2 = User(104, "user2", Some("x2"), Some(12345))
  private val user3 = User(105, "user3", Some("x3"), Some(23456), active = false)

  "UserRepo" should "find active/nonactive users by nickname" in {
    val action = for {
      _                        <- initTestDataSet
      foundByNameActive        <- userRepo.findByNickname("x3", Some(true))
      foundByNameNonActive     <- userRepo.findByNickname("x3", Some(false))
      foundByNameWithoutActive <- userRepo.findByNickname("x3")
    } yield (foundByNameActive, foundByNameNonActive, foundByNameWithoutActive)

    val result = action.transact(transactor).unsafeRunSync()
    result._1 should be(None)
    val foundByNameNonActive = result._2.getOrElse(fail("foundByNameNonActive should be present"))
    foundByNameNonActive.surname should be(user3.surname)
    val foundByNameWithoutActive = result._3.getOrElse(fail("foundByNameWithoutActive should be present"))
    foundByNameWithoutActive.surname should be(user3.surname)
  }

  it should "find by surnames and nicknames" in {
    val searchList1 = NonEmptyList("user1", "user2" :: Nil)
    val searchList2 = NonEmptyList("x2", "x3" :: Nil)
    val action = for {
      _      <- initTestDataSet
      first  <- userRepo.findBySurnames(searchList1)
      second <- userRepo.findByNicknames(searchList2)
    } yield (first, second)

    val (first, second) = action.transact(transactor).unsafeRunSync()
    first.size should be(2)
    second.size should be(2)
  }

  it should "find correctly by listAll" in {
    val action = for {
      _      <- initTestDataSet
      _      <- roleRepo.create(role)
      _      <- userRepo.assignUserToRealm(UserRealmRole(realm1.id, user1.id, role.id))
      first  <- userRepo.listAll(realm1.name, List("user1"))
      second <- userRepo.listAll(realm1.name, List("user1"), Some(false))
      third  <- userRepo.listAll(realm1.name, List("user2"))
    } yield (first, second, third)

    val (first, second, third) = action.transact(transactor).unsafeRunSync()
    first.size should be(1)
    first.head.id should be(user1.id)
    second.size should be(0)
    third.size should be(0)
  }

  it should "correctly find available Id" in {
    val action = for {
      _  <- initTestDataSet
      id <- userRepo.findAvailableId
    } yield id
    val id = action.transact(transactor).unsafeRunSync()
    id should be(4)
  }

  private def initTestDataSet: ConnectionIO[(User, User, User)] =
    for {
      _  <- createTestRealm(realm1)
      _  <- createTestRealm(realm2)
      u1 <- createTestUser(user1)
      u2 <- createTestUser(user2)
      u3 <- createTestUser(user3)
    } yield (u1, u2, u3)

  private def createTestUser(u: User): ConnectionIO[User] =
    userRepo.create(u)

  private def createTestRealm(realm: Realm): ConnectionIO[Realm] =
    realmRepo.create(realm)

  private def clearDBs(): Unit = {
    val action = for {
      _ <- userRepo.clearUserRealmRoles
      _ <- userRepo.removeN()
      _ <- realmRepo.removeN()
      _ <- roleRepo.removeN(List(105))
    } yield ()
    action.transact(transactor).attemptSql.unsafeRunSync()
  }

  override protected def beforeEach(): Unit = clearDBs()

  override protected def afterEach(): Unit = clearDBs()
}
