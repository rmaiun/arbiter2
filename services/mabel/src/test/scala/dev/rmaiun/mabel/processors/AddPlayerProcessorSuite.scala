package dev.rmaiun.mabel.processors

import cats.effect.IO
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.mabel.commands.AddPlayerCmd
import dev.rmaiun.mabel.dtos.BotRequest
import dev.rmaiun.mabel.services.ArbiterClient
import dev.rmaiun.mabel.utils.Loggable
import dev.rmaiun.protocol.http.UserDtoSet.{AssignUserToRealmDtoOut, RegisterUserDtoOut, UserDto}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}

class AddPlayerProcessorSuite
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterEach
    with OptionValues
    with Loggable {
  private val arbiterClient = mock(classOf[ArbiterClient[IO]])

  private val processor = AddPlayerProcessor.impl[IO](arbiterClient)

  "Game Points" should "create and list data successfully" in {
    val addUserResult = Flow.pure[IO, RegisterUserDtoOut](RegisterUserDtoOut(UserDto(24, "testuser")))
    val assignUserResult = Flow.pure[IO, AssignUserToRealmDtoOut](
      AssignUserToRealmDtoOut("testUser", "ua_test", "RegisteredUser", switchedAsActive = Some(true))
    )
    when(arbiterClient.addPlayer(any())).thenReturn(addUserResult)
    when(arbiterClient.assignUserToRealm(any)).thenReturn(assignUserResult)

    val dto   = AddPlayerCmd("ebobo", 1111, admin = false, 12345)
    val input = BotRequest("/addPlayer", 1234, 4444, "testuser", Some(AddPlayerCmd.AddPlayerCmdEncoder(dto)))
    val res   = processor.process(input).value.unsafeRunSync()
    res.isRight should be(true)
    val r = res.fold(_ => fail(), x => x)
    r.error should be(false)
    r.botResponse.result should include("New player was registered with id 24")
  }

}
