package dev.rmaiun.mabel.processors

import cats.effect.IO
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.mabel.commands.BroadcastMessageCmd
import dev.rmaiun.mabel.dtos.BotRequest
import dev.rmaiun.mabel.errors.Errors.NotEnoughRights
import dev.rmaiun.mabel.services.ArbiterClient
import dev.rmaiun.mabel.utils.Loggable
import dev.rmaiun.protocol.http.UserDtoSet.{ FindRealmAdminsDtoOut, UserRoleData }
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterEach, EitherValues }
import cats.effect.unsafe.implicits.global

class BroadcastMessageProcessorSuite
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterEach
    with EitherValues
    with Loggable {
  private val arbiterClient = mock(classOf[ArbiterClient[IO]])

  private val processor = BroadcastMessageProcessor.impl[IO](arbiterClient)

  "BroadCaseMessageProcessor" should "allow message broadcast" in {
    val userRoles = List(
      UserRoleData("user1", Some(1), "RealmAdmin"),
      UserRoleData("user2", Some(2), "RealmAdmin"),
      UserRoleData("user3", None, "Owner"),
      UserRoleData("user4", Some(4), "Admin")
    )
    val dtoOut = FindRealmAdminsDtoOut(userRoles)
    when(arbiterClient.findRealmAdmins(anyString())).thenReturn(Flow.pure(dtoOut))
    val dto = BroadcastMessageCmd("Test msg", 1)
    val input =
      BotRequest("broadcastMessage", 1234, 4444, "testuser", Some(BroadcastMessageCmd.BroadcastMessageCmdEncoder(dto)))
    val res = processor.process(input).value.unsafeRunSync()
    res.isRight should be(true)
    val r = res.fold(_ => fail(), x => x)
    r.fold(fail()) { x =>
      x.error should be(false)
      x.botResponse.result should include("Your message will be broadcast")
    }
  }

  it should "fail with NotEnoughRights error" in {
    val userRoles = List(
      UserRoleData("user4", Some(4), "Admin")
    )
    val dtoOut = FindRealmAdminsDtoOut(userRoles)
    when(arbiterClient.findRealmAdmins(anyString())).thenReturn(Flow.pure(dtoOut))
    val dto = BroadcastMessageCmd("Test msg", 1)
    val input =
      BotRequest("broadcastMessage", 1234, 4444, "testuser", Some(BroadcastMessageCmd.BroadcastMessageCmdEncoder(dto)))
    val res = processor.process(input).value.unsafeRunSync()
    res.isLeft should be(true)
    res.left.value shouldBe a[NotEnoughRights]
  }
}
