package dev.rmaiun.datamanager.repository

import cats.effect.{ ContextShift, IO }
import dev.rmaiun.datamanager.db.entities.Realm
import dev.rmaiun.datamanager.helpers.ConfigProvider.Config
import dev.rmaiun.datamanager.helpers.{ ConfigProvider, TransactorProvider }
import dev.rmaiun.datamanager.repositories.RealmRepo
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterEach, Inside }

class RealmRepoTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach with Inside {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  private val config: Config = ConfigProvider.provideConfig
  private val transactor: HikariTransactor[IO] =
    TransactorProvider.hikariTransactor[IO](config, allowPublicKeyRetrieval = true)
  private val realmRepo: RealmRepo[IO] = RealmRepo.impl[IO]

  "RealmRepo" should "insert new realm into db and successfully get it" in {

    val realm = Realm(0, "test_ua", 4, 1)

    val action = for {
      created <- createRealm(realm)
      found   <- findRealm(created.id)
    } yield found.map(f => (f.id, f.name))

    val result = action.transact(transactor).attemptSql.unsafeRunSync()
    result.isRight should be(true)
    val optData = result.getOrElse(fail("either was not Right!"))
    optData.isDefined should be(true)
    val data = optData.getOrElse(fail("either was not Right!"))
    data._1 should not be 0
    data._2 should be(realm.name)
  }

  private def createRealm(realm: Realm): ConnectionIO[Realm] =
    realmRepo.create(realm)
  private def findRealm(id: Long): ConnectionIO[Option[Realm]] =
    realmRepo.getById(id)
}
