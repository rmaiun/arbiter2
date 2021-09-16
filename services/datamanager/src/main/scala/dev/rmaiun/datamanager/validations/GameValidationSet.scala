package dev.rmaiun.datamanager.validations

import com.wix.accord.dsl._
import com.wix.accord.transform.ValidationTransform.TransformedValidator
import dev.rmaiun.datamanager.dtos.api.GameDtoSet._
import dev.rmaiun.validation.CustomValidationRules

object GameValidationSet extends CustomValidationRules {
  implicit val AddGameHistoryDtoInValidator: TransformedValidator[AddGameHistoryDtoIn] =
    validator[AddGameHistoryDtoIn] { dto =>
      dto.historyElement.realm is notEmpty and realm
      dto.historyElement.season is season
      dto.historyElement.w1 is sizeBetween(2, 20) and onlyLettersAndNumbers
      dto.historyElement.w2 is sizeBetween(2, 20) and onlyLettersAndNumbers
      dto.historyElement.l1 is sizeBetween(2, 20) and onlyLettersAndNumbers
      dto.historyElement.l2 is sizeBetween(2, 20) and onlyLettersAndNumbers
      dto.moderatorTid should be > 0L
    }

  implicit val UpdateRealmAlgorithmDtoInValidator: TransformedValidator[AddEloPointsDtoIn] =
    validator[AddEloPointsDtoIn] { dto =>
      dto.points.user is sizeBetween(2, 20) and onlyLettersAndNumbers
      dto.points.value should be > 0
      dto.moderatorTid should be > 0L
    }
}
