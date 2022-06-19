package dev.rmaiun.mabel.postprocessors

import cats.effect.IO
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.mabel.commands.BroadcastMessageCmd
import dev.rmaiun.mabel.dtos.BotRequest
import dev.rmaiun.mabel.postprocessor.BroadcastMessagePostProcessor
import dev.rmaiun.mabel.services.ArbiterClient
import dev.rmaiun.mabel.utils.Loggable
import dev.rmaiun.protocol.http.SeasonDtoSet.{ FindSeasonWithoutNotificationDtoOut, SeasonDto }
import dev.rmaiun.protocol.http.UserDtoSet._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.unsafe.implicits.global
import dev.rmaiun.mabel.helpers.PublisherProxy

class BroadcastMessagePostProcessorSuite extends AnyFlatSpec with Matchers with EitherValues with Loggable {
  "BroadcastMessagePostProcessor" should "send back message in testMode" in {
    checkBroadcastMessages(1, testMode = true)
  }

  it should "distribute N messages to users" in {
    checkBroadcastMessages(4)
  }

  private def checkBroadcastMessages(qty: Int, testMode: Boolean = false): Any = {
    val arbiterClient = mock(classOf[ArbiterClient[IO]])
    val publisher     = mock(classOf[PublisherProxy[IO]])
    val postProcessor = BroadcastMessagePostProcessor.impl[IO](arbiterClient, publisher)
    when(arbiterClient.findSeasonWithoutNotifications).thenReturn(
      Flow.pure(FindSeasonWithoutNotificationDtoOut(Some(SeasonDto(3, "S1|1999"))))
    )
    when(publisher.publishToBot(any())(any())).thenReturn(Flow.unit)
    when(arbiterClient.findAllPlayers).thenReturn(
      Flow.pure(
        FindAllUsersDtoOut(
          List(
            UserDto(1, "x", tid = Some(1)),
            UserDto(1, "y"),
            UserDto(1, "w", tid = Some(2)),
            UserDto(1, "z", tid = Some(3)),
            UserDto(1, "k", tid = Some(4)),
            UserDto(1, "p")
          )
        )
      )
    )
    val dto = BroadcastMessageCmd("Test msg", 1, testMode)
    val input =
      BotRequest("broadcastMessage", 1234, 4444, "testuser", Some(BroadcastMessageCmd.BroadcastMessageCmdEncoder(dto)))
    val res = postProcessor.postProcess(input).value.unsafeRunSync()
    verify(publisher, times(qty)).publishToBot(any())(any())
    res.isRight should be(true)
  }
}
