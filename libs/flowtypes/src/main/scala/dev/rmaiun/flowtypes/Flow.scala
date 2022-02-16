package dev.rmaiun.flowtypes

import cats.data.EitherT
import cats.effect.MonadCancel
import cats.implicits._
import cats.{Applicative, Functor, Monad}

object Flow {
  type ErrorOr[T]           = Either[Throwable, T]
  type Flow[F[_], T]        = EitherT[F, Throwable, T]
  type MonadThrowable[F[_]] = MonadCancel[F, Throwable]

  def right[F[_]: Applicative, R](data: R): F[ErrorOr[R]] =
    Applicative[F].pure(data.asRight[Throwable])

  def left[F[_]: Applicative, R](data: Throwable): F[ErrorOr[R]] =
    Applicative[F].pure(data.asLeft[R])

  def fromEither[F[_]: Applicative, T](data: ErrorOr[T]): Flow[F, T] =
    EitherT(Applicative[F].pure(data))

  def pure[F[_]: Applicative, T](data: T): Flow[F, T] =
    EitherT(Applicative[F].pure(data.asRight[Throwable]))

  def error[F[_]: Applicative, R](data: Throwable): Flow[F, R] =
    EitherT(Applicative[F].pure(data.asLeft[R]))

  def liftEither[F[_]: Applicative, T](data: ErrorOr[T]): Flow[F, T] =
    EitherT(Applicative[F].pure(data))

  def fromFEither[F[_], T](f: F[ErrorOr[T]]): Flow[F, T] =
    EitherT(f)

  def fromFOpt[F[_]: Functor, T](f: F[Option[T]], err: Throwable): Flow[F, T] =
    EitherT.fromOptionF(f, err)

  def fromOpt[F[_]: Applicative, T](f: Option[T], err: Throwable): Flow[F, T] =
    EitherT.fromOption(f, err)

  def effect[F[_]: Monad, T](fa: F[T])(implicit ME: MonadThrowable[F]): Flow[F, T] = {
    val result = fa.map(_.asRight[Throwable]).recoverWith { case e =>
      ME.pure(e.asLeft[T])
    }
    Flow.fromFEither(result)
  }

  def unit[F[_]: Monad]: Flow[F, Unit] = Flow.pure(())
}
