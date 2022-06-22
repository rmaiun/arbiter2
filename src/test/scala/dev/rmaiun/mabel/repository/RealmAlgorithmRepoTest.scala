package dev.rmaiun.arbiter2.repository

import cats.effect.IO
import dev.rmaiun.arbiter2.db.entities.{ Algorithm, Realm }
import dev.rmaiun.arbiter2.helpers.ConfigProvider.Config
import dev.rmaiun.arbiter2.helpers.{ ConfigProvider, TransactorProvider }
import dev.rmaiun.arbiter2.repositories.{ AlgorithmRepo, RealmRepo }
import dev.rmaiun.arbiter2.utils.IoTestRuntime
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterEach, Inside }

import scala.util.Random

class RealmAlgorithmRepoTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach with Inside with IoTestRuntime {

  private val config: Config = ConfigProvider.provideConfig
  private val transactor: HikariTransactor[IO] =
    TransactorProvider.hikariTransactor[IO](config, allowPublicKeyRetrieval = true)
  private val realmRepo: RealmRepo[IO]   = RealmRepo.impl[IO]
  private val algRepo: AlgorithmRepo[IO] = AlgorithmRepo.impl[IO]

  "Realm and Algorithm Repos" should "insert new algorithm and realm into db and successfully get it" in {
    val action = for {
      alg     <- createTestAlgorithm("TestAlgo")
      created <- createRealm(alg)
      found   <- findRealm(created.id)
    } yield found.map(f => (f.id, f.name, alg))

    val result = action.transact(transactor).attemptSql.unsafeRunSync()
    result.isRight should be(true)
    val optData = result.getOrElse(fail("either was not Right!"))
    optData.isDefined should be(true)
    val data = optData.getOrElse(fail("either was not Right!"))
    data._1 should not be 0
    data._2 should be("test_ua")
    data._3.id should not be 0
    data._3.value should be("TestAlgo")
  }

  it should "successfully update Algorithm" in {
    val alg = createTestAlgorithm("TestAlgo2").transact(transactor).attemptSql.unsafeRunSync()
    alg.isRight should be(true)
    val result = alg.getOrElse(fail("alg either was not Right!"))
    result.id should not be 0
    val updAlg    = algRepo.update(result.copy(value = "Elo")).transact(transactor).attemptSql.unsafeRunSync()
    val updResult = updAlg.getOrElse(fail("updAlg either was not Right!"))
    updResult.id shouldEqual result.id
    updResult.value should not equal result.value
  }

  it should " successfully update Realm" in {
    val action = for {
      alg <- createTestAlgorithm("TestAlgo3")
      r   <- createRealm(alg)
    } yield r
    val realmCreated = action.transact(transactor).attemptSql.unsafeRunSync()
    realmCreated.isRight should be(true)
    val result = realmCreated.getOrElse(fail("realmCreated either was not Right!"))
    result.id should not be 0
    val realmUpd =
      realmRepo.update(result.copy(name = "some_realm", teamSize = 6)).transact(transactor).attemptSql.unsafeRunSync()
    val updResult = realmUpd.getOrElse(fail("realmUpd either was not Right!"))
    updResult.id shouldEqual result.id
    updResult.name should be("some_realm")
    updResult.teamSize should be(6)
  }

  it should "successfully delete 2 of 3 realms" in {
    val action = for {
      alg <- createTestAlgorithm("TestAlgo")
      r   <- createRealm(alg)
      r2  <- realmRepo.create(r.copy(name = "realm2", id = 101))
      r3  <- realmRepo.create(r.copy(name = "realm3", id = 102))
    } yield (r, r2, r3)
    val realmsCreated = action.transact(transactor).attemptSql.unsafeRunSync()
    realmsCreated.isRight should be(true)
    val result = realmsCreated.getOrElse(fail("realmsCreated either was not Right!"))
    List(result._1.id, result._2.id, result._3.id) should not contain 0
    val listAll    = realmRepo.listAll.transact(transactor).attemptSql.unsafeRunSync()
    val listAllRes = listAll.getOrElse(fail("listAll either was not Right!"))
    listAllRes.size should be(5)
    val delete2Elems = realmRepo
      .removeN(List(result._2.id, result._3.id))
      .transact(transactor)
      .attemptSql
      .unsafeRunSync()
    delete2Elems.isRight should be(true)
    val deleteValue = delete2Elems.getOrElse(fail("delete2Elems either was not Right!"))
    deleteValue shouldEqual 2
    val listAll2    = realmRepo.listAll.transact(transactor).attemptSql.unsafeRunSync()
    val listAllRes2 = listAll2.getOrElse(fail("listAll2 either was not Right!"))
    listAllRes2.size should be(3)
    listAllRes2.map(_.name) should contain("test_ua")
  }

  private def createTestAlgorithm(name: String): ConnectionIO[Algorithm] =
    algRepo.create(Algorithm(Random.nextLong(100) + 1, name))
  private def createRealm(alg: Algorithm): ConnectionIO[Realm] =
    realmRepo.create(Realm(Random.nextLong(100) + 1, "test_ua", 4, alg.id))
  private def findRealm(id: Long): ConnectionIO[Option[Realm]] =
    realmRepo.getById(id)

  private def clearRealAlgorithmDBs(): Unit = {
    val action = for {
      _ <- realmRepo.removeN(3L.to(105L).toList)
      _ <- algRepo.removeN(2L.to(105L).toList)
    } yield ()
    action.transact(transactor).attemptSql.unsafeRunSync()
  }

  override protected def beforeEach(): Unit = clearRealAlgorithmDBs()

  override protected def afterEach(): Unit = clearRealAlgorithmDBs()
}
