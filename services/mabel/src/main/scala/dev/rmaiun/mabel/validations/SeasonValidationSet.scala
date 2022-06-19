package dev.rmaiun.mabel.validations

import com.wix.accord.dsl._
import com.wix.accord.transform.ValidationTransform.TransformedValidator
import dev.rmaiun.protocol.http.SeasonDtoSet.{
  CreateSeasonDtoIn,
  FindSeasonWithoutNotificationDtoIn,
  NotifySeasonDtoIn
}
import dev.rmaiun.validation.CustomValidationRules

object SeasonValidationSet extends CustomValidationRules {
  implicit val CreateSeasonDtoInValidator: TransformedValidator[CreateSeasonDtoIn] = validator[CreateSeasonDtoIn] {
    dto =>
      dto.name is season
      dto.algorithm.each is oneOf("WinLoss", "OldPoints")
      dto.realm is realm
  }

  implicit val FindSeasonWithoutNotificationDtoInValidator: TransformedValidator[FindSeasonWithoutNotificationDtoIn] =
    validator[FindSeasonWithoutNotificationDtoIn] { dto =>
      dto.realm is realm
    }

  implicit val NotifySeasonDtoInValidator: TransformedValidator[NotifySeasonDtoIn] =
    validator[NotifySeasonDtoIn] { dto =>
      dto.season is season
      dto.realm is realm

    }
}
