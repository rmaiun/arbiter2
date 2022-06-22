package dev.rmaiun.arbiter2.processors

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import dev.rmaiun.arbiter2.commands.LastGamesCmd
import dev.rmaiun.arbiter2.dtos.BotRequest
import dev.rmaiun.arbiter2.managers.GameManager
import dev.rmaiun.arbiter2.processors.LastGamesProcessor
import dev.rmaiun.common.{ DateFormatter, SeasonHelper }
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.arbiter2.utils.Loggable
import dev.rmaiun.protocol.http.GameDtoSet.{ ListGameHistoryDtoOut, StoredGameHistoryDto }
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{ mock, when }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{ BeforeAndAfterEach, OptionValues }

class ListGamesProcessorSuite
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterEach
    with OptionValues
    with Loggable {

  private val gameManager = mock(classOf[GameManager[IO]])
  private val processor   = LastGamesProcessor.impl[IO](gameManager)

  "Last Games" should "should work correctly for [games > 0] " in {
    when(gameManager.listGameHistory(any()))
      .thenReturn(Flow.pure(ListGameHistoryDtoOut(prepareGameHistoryList(0))))
    val dto   = LastGamesCmd()
    val input = BotRequest("lastGames", 1234, 4444, "testuser", Some(LastGamesCmd.LastGamesCmdCodec(dto)))
    val res   = processor.process(input).value.unsafeRunSync()
    val r     = res.fold(_ => fail(), x => x)
    r.value.error should be(false)
    r.value.botResponse.result should include("Last 10 games")
    r.value.botResponse.result should include(s"No games found for season ${SeasonHelper.currentSeason}")
  }

  it should "should work correctly for [0 < games < 10]" in {
    when(gameManager.listGameHistory(any()))
      .thenReturn(Flow.pure(ListGameHistoryDtoOut(prepareGameHistoryList(4))))
    val dto   = LastGamesCmd(Some(SeasonHelper.currentSeason))
    val input = BotRequest("lastGames", 1234, 4444, "testuser", Some(LastGamesCmd.LastGamesCmdCodec(dto)))
    val res   = processor.process(input).value.unsafeRunSync()
    val r     = res.fold(_ => fail(), x => x)
    r.value.error should be(false)
    r.value.botResponse.result should include("Last 10 games")
    for (a <- 0 to 3) {
      r.value.botResponse.result should include(s"A$a")
    }
  }

  it should "should work correctly for [games > 10]" in {
    when(gameManager.listGameHistory(any()))
      .thenReturn(Flow.pure(ListGameHistoryDtoOut(prepareGameHistoryList(56))))
    val dto   = LastGamesCmd()
    val input = BotRequest("lastGames", 1234, 4444, "testuser", Some(LastGamesCmd.LastGamesCmdCodec(dto)))
    val res   = processor.process(input).value.unsafeRunSync()
    val r     = res.fold(_ => fail(), x => x)
    r.value.error should be(false)
    r.value.botResponse.result should include("Last 10 games")
    for (a <- 45 to 55) {
      r.value.botResponse.result should include(s"A$a")
    }
  }

  private def prepareGameHistoryList(qty: Int): List[StoredGameHistoryDto] = {
    val now = DateFormatter.now
    (0 until qty)
      .map(a =>
        StoredGameHistoryDto(
          "testRealm",
          SeasonHelper.currentSeason,
          s"a$a",
          "b",
          "c",
          "d",
          shutout = false,
          now.plusSeconds(a)
        )
      )
      .toList
  }
}
