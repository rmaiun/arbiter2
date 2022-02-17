package dev.rmaiun.soos.helpers

import dev.rmaiun.soos.utils.{ IoTestRuntime, TestModule }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

case class ZipDataProviderTest() extends AnyFlatSpec with Matchers with IoTestRuntime {

  "RoleRepo" should "insert new role into db and successfully get it" in {
    val result = TestModule.zipDataProvider.exportArchive.value.unsafeRunSync()
    result.isRight should be(true)
  }
}
