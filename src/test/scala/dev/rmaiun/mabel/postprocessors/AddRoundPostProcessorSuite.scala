package dev.rmaiun.arbiter2.postprocessors

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import dev.rmaiun.arbiter2.commands.AddRoundCmd
import dev.rmaiun.arbiter2.dtos.BotRequest
import dev.rmaiun.arbiter2.helpers.PublisherProxy
import dev.rmaiun.arbiter2.managers.UserManager
import dev.rmaiun.arbiter2.postprocessor.AddRoundPostProcessor
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.arbiter2.dtos.CmdType.ADD_ROUND_CMD
import dev.rmaiun.arbiter2.utils.Loggable
import dev.rmaiun.protocol.http.UserDtoSet.{ FindRealmAdminsDtoOut, FindUserDtoOut, UserDto, UserRoleData }
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AddRoundPostProcessorSuite extends AnyFlatSpec with Matchers with EitherValues with Loggable {
  private val userManager   = mock(classOf[UserManager[IO]])
  private val publisher     = mock(classOf[PublisherProxy[IO]])
  private val postProcessor = AddRoundPostProcessor.impl[IO](userManager, publisher)

  "AddRoundPostProcessor" should "send right number of messages" in {
    when(publisher.publishToBot(any())(any())).thenReturn(Flow.unit)
    val userRoles = List(
      UserRoleData("user1", Some(1), "RealmAdmin"),
      UserRoleData("user2", Some(2), "RealmAdmin"),
      UserRoleData("user3", None, "Owner"),
      UserRoleData("user4", Some(4), "Admin")
    )
    val dtoOut = FindRealmAdminsDtoOut(userRoles)
    when(userManager.findRealmAdmins(any())).thenReturn(Flow.pure(dtoOut))
    val findUserDto = FindUserDtoOut(UserDto(1, "test", tid = Some(4)), Nil)
    when(userManager.findUser(any())).thenReturn(Flow.pure(findUserDto))
    val addRoundCmd = AddRoundCmd("x1", "x2", "y1", "y2", shutout = false, 2)
    val input       = BotRequest(ADD_ROUND_CMD, 2, 2, "user2", Some(AddRoundCmd.AddRoundCmdCodec(addRoundCmd)))
    val res         = postProcessor.postProcess(input).value.unsafeRunSync()
    verify(publisher, times(6)).publishToBot(any())(any())
    res.value should be(())
  }
}
