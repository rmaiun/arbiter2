package dev.rmaiun.datamanager.validations
import com.wix.accord.dsl._
import com.wix.accord.transform.ValidationTransform.TransformedValidator
import dev.rmaiun.datamanager.dtos.RealmDtos.{ DropRealmDtoIn, RegisterRealmDtoIn, UpdateRealmAlgorithmDtoIn }
import dev.rmaiun.validation.CustomValidationRules

object RealmValidationSet extends CustomValidationRules {
  implicit val RegisterRealmDtoInValidator: TransformedValidator[RegisterRealmDtoIn] = validator[RegisterRealmDtoIn] {
    dto =>
      dto.realmName is notEmpty and sizeBetween(2, 20)
      dto.algorithm is oneOf("WinRate", "OldPoints")
  }

  implicit val UpdateRealmAlgorithmDtoInValidator: TransformedValidator[UpdateRealmAlgorithmDtoIn] =
    validator[UpdateRealmAlgorithmDtoIn] { dto =>
      dto.algorithm is oneOf("WinRate", "OldPoints")
    }

  implicit val DropRealmDtoInValidator: TransformedValidator[DropRealmDtoIn] = validator[DropRealmDtoIn] { dto =>
    dto.id should be > 0L
  }
}
