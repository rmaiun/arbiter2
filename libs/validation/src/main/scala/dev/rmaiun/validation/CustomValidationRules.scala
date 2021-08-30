package dev.rmaiun.validation

import com.wix.accord.{ BaseValidator, NullSafeValidator, Validator }
import com.wix.accord.ViolationBuilder._

trait CustomValidationRules {

  def oneOf[T <: AnyRef](options: T*): Validator[T] =
    new NullSafeValidator[T](
      test = options.contains,
      failure = _ -> s"is not one of (${options.mkString(",")})"
    )

  def sizeBetween(from: Int, to: Int): Validator[String] =
    new NullSafeValidator[String](
      test = str => str.length >= from && str.length <= to,
      failure = _ -> s"must have length between ($from, $to)"
    )

  def onlyNumbers: Validator[String] = new NullSafeValidator[String](
    test = data => data.forall(c => Character.isDigit(c)),
    failure = _ -> "must contain only numbers"
  )

  def onlyLetters: Validator[String] = new NullSafeValidator[String](
    test = data => data.forall(c => Character.isLetter(c)),
    failure = _ -> "must contain only letters"
  )

  def season: Validator[String] = new NullSafeValidator[String](
    test = data => data.length == 7 && data.matches("^[Ss][1-4]\\|\\d{4}$"),
    failure = _ -> "must match season pattern 'S{1-4}|yyyy'"
  )

  def intBetween(from: Int, to: Int): Validator[Int] = new BaseValidator[Int](
    test = data => data >= from && data <= to,
    failure = _ -> s"must be in range [$from, $to]"
  )

  def realm: Validator[String] = sizeBetween(5,20)
}
