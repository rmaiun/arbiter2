package dev.rmaiun.validation

import cats.Monad
import com.wix.accord._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.validation.errors.ValidationRuntimeException


object Validator {
  def validateDto[F[_]:Monad, T](data: T)(implicit v: Validator[T]): Flow[F, Unit] = {
    val vr = validate(data)(v)
    vr match {
      case Success => Flow.unit
      case Failure(violations) =>
        val errorMessages = violations.map(v => {
          val field = v.path.head match {
            case description: Descriptions.AssociativeDescription => description
            case Descriptions.Explicit(description) => description
            case Descriptions.Generic(description) => description
          }
          s"$field" -> s"${v.constraint}"
        }).toMap
        Flow.error(ValidationRuntimeException(errorMessages))
    }
  }
}
