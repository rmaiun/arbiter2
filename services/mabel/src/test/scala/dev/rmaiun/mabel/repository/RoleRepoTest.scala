package dev.rmaiun.mabel.repository

import cats.effect.IO
import dev.rmaiun.mabel.db.entities.Role
import dev.rmaiun.mabel.helpers.ConfigProvider.Config
import dev.rmaiun.mabel.helpers.{ ConfigProvider, TransactorProvider }
import dev.rmaiun.mabel.repositories.RoleRepo
import dev.rmaiun.mabel.utils.IoTestRuntime
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterEach, OptionValues }

import java.time.ZonedDateTime

class RoleRepoTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach with OptionValues with IoTestRuntime {

  private val config: Config = ConfigProvider.provideConfig
  private val transactor: HikariTransactor[IO] =
    TransactorProvider.hikariTransactor[IO](config, allowPublicKeyRetrieval = true)
  private val roleRepo: RoleRepo[IO] = RoleRepo.impl[IO]
  private val role                   = Role(101, "xxx", 50)

  "RoleRepo" should "insert new role into db and successfully get it" in {
    val result = createRole(role).transact(transactor).unsafeRunSync()
    result.id should not be 0
    result.value should be(role.value)
    result.permission should be(role.permission)
  }

  it should "should successfully update role" in {
    val now         = ZonedDateTime.now()
    val createdRole = createRole(role).transact(transactor).unsafeRunSync()
    val updRole = roleRepo
      .update(createdRole.copy(permission = 99))
      .transact(transactor)
      .unsafeRunSync()
    updRole.permission should be(99)
  }

  it should "successfully delete 1 of 3 roles" in {
    val action = for {
      r  <- createRole(role)
      r2 <- createRole(role.copy(value = "yyy", id = 102))
      r3 <- createRole(role.copy(value = "zzz", id = 103))
    } yield (r, r2, r3)
    val realmsCreated = action.transact(transactor).unsafeRunSync()

    List(realmsCreated._1.id, realmsCreated._2.id, realmsCreated._3.id) should not contain 0
    val listAll = roleRepo.listAll.transact(transactor).unsafeRunSync()
    listAll.size should be(8)
    val delete2Elems = roleRepo
      .removeN(List(realmsCreated._1.id, realmsCreated._2.id))
      .transact(transactor)
      .unsafeRunSync()
    delete2Elems shouldEqual 2
    val listAll2 = roleRepo.listAll.transact(transactor).unsafeRunSync()
    listAll2.size should be(6)
  }

  private def createRole(role: Role): ConnectionIO[Role] = roleRepo.create(role)

  private def clearRealAlgorithmDBs(): Unit = {
    val action = for {
      _ <- roleRepo.removeN(List(101, 102, 103))
    } yield ()
    action.transact(transactor).attemptSql.unsafeRunSync()
  }

  override protected def beforeEach(): Unit = clearRealAlgorithmDBs()

  override protected def afterEach(): Unit = clearRealAlgorithmDBs()
}
