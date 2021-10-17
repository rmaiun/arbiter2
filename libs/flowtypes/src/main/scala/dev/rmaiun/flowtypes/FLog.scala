package dev.rmaiun.flowtypes

import cats.Functor
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import dev.rmaiun.flowtypes.Flow.Flow
import io.chrisdavenport.log4cats.Logger

object FLog {
  def info[F[_]: Logger: Functor](msg: String): Flow[F, Unit] =
    EitherT(Functor[F].map(Logger[F].info(msg))(_.asRight[Throwable]))

  def warn[F[_]: Logger: Functor](msg: String): Flow[F, Unit] =
    EitherT(Functor[F].map(Logger[F].warn(msg))(_.asRight[Throwable]))

  def error[F[_]: Logger: Functor](msg: String): Flow[F, Unit] =
    EitherT(Functor[F].map(Logger[F].error(msg))(_.asRight[Throwable]))

  def error[F[_]: Logger: Functor](cause: Throwable): Flow[F, Unit] =
    EitherT(Functor[F].map(Logger[F].error(cause)("Error was triggered"))(_.asRight[Throwable]))
}
