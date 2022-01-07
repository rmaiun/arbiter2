package dev.rmaiun.serverauth.middleware

import cats.Monad
import cats.data.{ Kleisli, OptionT }
import dev.rmaiun.serverauth.dto.AuthUser
import org.http4s.Request

object Auth {
  def authUser[F[_]: Monad]: Kleisli[OptionT[F, *], Request[F], AuthUser] = ???
//  Kleisli(x => )
}
