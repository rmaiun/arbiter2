package dev.rmaiun.mabel.postprocessors

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.mabel.commands.AddPlayerCmd
import dev.rmaiun.mabel.dtos.BotRequest
import dev.rmaiun.mabel.dtos.CmdType.ADD_PLAYER_CMD
import dev.rmaiun.mabel.helpers.PublisherProxy
import dev.rmaiun.mabel.managers.UserManager
import dev.rmaiun.mabel.postprocessor.AddPlayerPostProcessor
import dev.rmaiun.mabel.utils.Loggable
import dev.rmaiun.protocol.http.UserDtoSet.{ FindRealmAdminsDtoOut, FindUserDtoOut, UserDto, UserRoleData }
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AddPlayerPostProcessorSuite extends AnyFlatSpec with Matchers with EitherValues with Loggable {
  private val publisher     = mock(classOf[PublisherProxy[IO]])
  private val userManager   = mock(classOf[UserManager[IO]])
  private val postProcessor = AddPlayerPostProcessor.impl[IO](userManager, publisher)

  "AddPlayerPostProcessor" should "send right number of messages" in {
    when(publisher.publishToBot(any())(any())).thenReturn(Flow.unit)
    val userRoles = List(
      UserRoleData("user1", Some(1), "RealmAdmin"),
      UserRoleData("user2", Some(2), "RealmAdmin"),
      UserRoleData("user3", None, "Owner")
    )
    val dtoOut = FindRealmAdminsDtoOut(userRoles)
    when(userManager.findRealmAdmins(any())).thenReturn(Flow.pure(dtoOut))
    val findUserDto = FindUserDtoOut(UserDto(1, "test", tid = Some(4)), Nil)
    when(userManager.findUser(any())).thenReturn(Flow.pure(findUserDto))
    val addPlayerCmd = AddPlayerCmd("x", Some(1), admin = false, 2)
    val input        = BotRequest(ADD_PLAYER_CMD, 2, 2, "user2", Some(AddPlayerCmd.AddPlayerCmdCodec(addPlayerCmd)))
    val res          = postProcessor.postProcess(input).value.unsafeRunSync()
    verify(publisher, times(2)).publishToBot(any())(any())
    res.value should be(())
  }
}
