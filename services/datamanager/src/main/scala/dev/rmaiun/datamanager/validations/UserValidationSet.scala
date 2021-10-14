package dev.rmaiun.datamanager.validations

import com.wix.accord.dsl._
import com.wix.accord.transform.ValidationTransform.TransformedValidator
import dev.rmaiun.protocol.http.UserDtoSet._
import dev.rmaiun.validation.CustomValidationRules

object UserValidationSet extends CustomValidationRules {

  implicit val RegisterUserDtoInValidator: TransformedValidator[RegisterUserDtoIn] = validator[RegisterUserDtoIn] {
    dto =>
      dto.user.surname is sizeBetween(2, 20)
      dto.user.tid.each should be > 0L
      dto.moderatorTid should be > 0L
  }

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
      dto.role.each should sizeBetween(4, 20)
    }

  implicit val SwitchActiveRealmDtoInValidator: TransformedValidator[SwitchActiveRealmDtoIn] =
    validator[SwitchActiveRealmDtoIn] { dto =>
      dto.realm is notBlank and realm
      dto.user is notBlank and sizeBetween(2, 20)
    }

  implicit val ProcessActivationDtoInValidator: TransformedValidator[ProcessActivationDtoIn] =
    validator[ProcessActivationDtoIn] { dto =>
      dto.users.each should sizeBetween(2, 20)
      dto.moderatorTid should be > 0L
    }

  implicit val LinkTidDtoInValidator: TransformedValidator[LinkTidDtoIn] =
    validator[LinkTidDtoIn] { dto =>
      dto.tid should be > 0L
      dto.moderatorTid should be > 0L
      dto.nameToLink is notBlank and sizeBetween(2, 20)
      dto.realm is notBlank and realm
    }

  implicit val FindAvailableRealmsDtoInValidator: TransformedValidator[FindAvailableRealmsDtoIn] =
    validator[FindAvailableRealmsDtoIn] { dto =>
      dto.surname is notBlank and sizeBetween(2, 20)
    }
}
