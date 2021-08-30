package dev.rmaiun.datamanager.validations

import com.wix.accord.dsl._
import com.wix.accord.transform.ValidationTransform.TransformedValidator
import dev.rmaiun.datamanager.dtos.api.UserDtos.{AssignUserToRealmDtoIn, FindAllUsersDtoIn, FindUserDtoIn, ProcessActivationDtoIn, SwitchActiveRealmDtoIn}
import dev.rmaiun.validation.CustomValidationRules

object UserValidationSet extends CustomValidationRules {
  implicit val FindUserDtoInValidator: TransformedValidator[FindUserDtoIn] = validator[FindUserDtoIn] { dto =>
    dto.surname.each is sizeBetween(2, 20)
    dto.tid.each should be > 0L
  }

  implicit val FindAllUsersDtoInValidator: TransformedValidator[FindAllUsersDtoIn] = validator[FindAllUsersDtoIn] {
    dto =>
      dto.realm is notBlank and realm
  }

  implicit val AssignUserToRealmDtoInValidator: TransformedValidator[AssignUserToRealmDtoIn] =
    validator[AssignUserToRealmDtoIn] { dto =>
      dto.realm is notBlank and realm
      dto.user is notBlank and sizeBetween(2, 20)
      dto.role.each is notBlank and sizeBetween(4, 20)
    }

  implicit val SwitchActiveRealmDtoInValidator: TransformedValidator[SwitchActiveRealmDtoIn] =
    validator[SwitchActiveRealmDtoIn] { dto =>
      dto.realm is notBlank and realm
      dto.user is notBlank and sizeBetween(2, 20)
    }

  implicit val ProcessActivationDtoInValidator: TransformedValidator[ProcessActivationDtoIn] =
    validator[ProcessActivationDtoIn] { dto =>
      dto.users.each is notBlank and sizeBetween(2, 20)
      dto.moderatorTid should be > 0L
    }
}
