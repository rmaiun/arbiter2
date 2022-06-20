package dev.rmaiun.mabel.postprocessors

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import dev.rmaiun.common.DateFormatter
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.mabel.dtos.BotRequest
import dev.rmaiun.mabel.dtos.CmdType.SEASON_RESULTS_CMD
import dev.rmaiun.mabel.helpers.ConfigProvider.AppConfig
import dev.rmaiun.mabel.helpers.PublisherProxy
import dev.rmaiun.mabel.managers.{ GameManager, SeasonManager, UserManager }
import dev.rmaiun.mabel.postprocessor.SeasonResultPostProcessor
import dev.rmaiun.mabel.utils.Loggable
import dev.rmaiun.protocol.http.GameDtoSet.{ ListGameHistoryDtoOut, StoredGameHistoryDto }
import dev.rmaiun.protocol.http.SeasonDtoSet.{ FindSeasonWithoutNotificationDtoOut, NotifySeasonDtoOut, SeasonDto }
import dev.rmaiun.protocol.http.UserDtoSet._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SeasonResultsPostProcessorSuite extends AnyFlatSpec with Matchers with EitherValues with Loggable {
  "SeasonResultsPostProcessor" should "send right number of messages for unrated players" in {
    testGameHistoryQty(4)
  }

  "SeasonResultsPostProcessor" should "send right number of messages for combined players" in {
    testGameHistoryQty(31)
  }

  private def testGameHistoryQty(qty: Int): Any = {
    val seasonManager = mock(classOf[SeasonManager[IO]])
    val gameManager   = mock(classOf[GameManager[IO]])
    val userManager   = mock(classOf[UserManager[IO]])
    val publisher     = mock(classOf[PublisherProxy[IO]])
    val appCfg        = AppConfig(notifications = true, "Europe/Kiev", "", "", 20, 20, 20, "", "", 1)
    val postProcessor = SeasonResultPostProcessor.impl[IO](seasonManager, gameManager, userManager, publisher, appCfg)
    val now           = DateFormatter.now(appCfg.reportTimezone)
    when(seasonManager.findSeasonWithoutNotification(any())).thenReturn(
      Flow.pure(FindSeasonWithoutNotificationDtoOut(Some(SeasonDto(3, "S1|1999"))))
    )
    when(publisher.publishToBot(any())(any())).thenReturn(Flow.unit)
    when(seasonManager.notifySeason(any())).thenReturn(Flow.pure(NotifySeasonDtoOut("notImportant", DateFormatter.now)))
    val data = (1 to qty)
      .map(_ => StoredGameHistoryDto("x1", "S1|1999", "x", "y", "w", "z", shutout = false, DateFormatter.now))
      .toList
    val finalData = data.head.copy(l1 = "k", l2 = "p") :: data
    when(gameManager.listGameHistory(any()))
      .thenReturn(Flow.pure(ListGameHistoryDtoOut(finalData)))
    when(userManager.findAllUsers(any())).thenReturn(
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
    val input = BotRequest(SEASON_RESULTS_CMD, 2, 2, "user2")
    val res   = postProcessor.postProcess(input).value.unsafeRunSync()
    if (now.getHour >= 10 && now.getHour <= 23) {
      verify(publisher, times(4)).publishToBot(any())(any())
    } else {
      verify(publisher, never()).publishToBot(any())(any())
    }
    res.value should be(())
  }
}
