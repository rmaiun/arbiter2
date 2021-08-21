package dev.rmaiun.datamanager.validations

import com.wix.accord.dsl._
import com.wix.accord.transform.ValidationTransform.TransformedValidator
import dev.rmaiun.datamanager.dtos.api.AlgorithmDtos.{ CreateAlgorithmDtoIn, DeleteAlgorithmDtoIn, GetAlgorithmDtoIn }
import dev.rmaiun.validation.CustomValidationRules

object AlgorithmValidationSet extends CustomValidationRules {
  implicit val GetAlgorithmDtoInValidator: TransformedValidator[GetAlgorithmDtoIn] = validator[GetAlgorithmDtoIn] {
    dto =>
      dto.algorithm is notEmpty and sizeBetween(2, 20)
  }

  implicit val CreateAlgorithmDtoInValidator: TransformedValidator[CreateAlgorithmDtoIn] =
    validator[CreateAlgorithmDtoIn] { dto =>
      dto.algorithm is oneOf("WinRate", "OldPoints")
    }

  implicit val DeleteAlgorithmDtoInValidator: TransformedValidator[DeleteAlgorithmDtoIn] =
    validator[DeleteAlgorithmDtoIn] { dto =>
      dto.id should be > 0L
    }
}
