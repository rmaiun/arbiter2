package dev.rmaiun.flowtypes

import cats.data.EitherT
import cats.implicits._
import cats.{Applicative, Functor, Monad, MonadError}
import io.chrisdavenport.log4cats.Logger

object Flow {
  type ErrorOr[T]           = Either[Throwable, T]
  type Flow[F[_], T]        = EitherT[F, Throwable, T]
  type MonadThrowable[F[_]] = MonadError[F, Throwable]
  object FlowLog {
    def info[F[_]: Logger: Functor](msg: String): Flow[F, Unit] =
      EitherT(Functor[F].map(Logger[F].info(msg))(_.asRight[Throwable]))

    def warn[F[_]: Logger: Functor](msg: String): Flow[F, Unit] =
      EitherT(Functor[F].map(Logger[F].warn(msg))(_.asRight[Throwable]))

    def error[F[_]: Logger: Functor](msg: String): Flow[F, Unit] =
      EitherT(Functor[F].map(Logger[F].error(msg))(_.asRight[Throwable]))
  }

  def toRightResult[F[_]: Applicative, R](data: R): F[ErrorOr[R]] =
    Applicative[F].pure(data.asRight[Throwable])

  def toLeftResult[F[_]: Applicative, R](data: Throwable): F[ErrorOr[R]] =
    Applicative[F].pure(data.asLeft[R])

  def fromResult[F[_]: Applicative, T](data: ErrorOr[T]): Flow[F, T] =
    EitherT(Applicative[F].pure(data))

  def pure[F[_]: Applicative, T](data: T): Flow[F, T] =
    EitherT(Applicative[F].pure(data.asRight[Throwable]))

  def error[F[_]: Applicative, R](data: Throwable): Flow[F, R] =
    EitherT(Applicative[F].pure(data.asLeft[R]))

  def liftRes[F[_]: Applicative, T](data: ErrorOr[T]): Flow[F, T] =
    EitherT(Applicative[F].pure(data))

  def fromFRes[F[_], T](f: F[ErrorOr[T]]): Flow[F, T] =
    EitherT(f)

  def fromFOpt[F[_]: Functor, T](f: F[Option[T]], err: Throwable): Flow[F, T] =
    EitherT.fromOptionF(f, err)

  def fromOpt[F[_]: Applicative, T](f: Option[T], err: Throwable): Flow[F, T] =
    EitherT.fromOption(f, err)

  def fromF[F[_]: Monad, T](fa: F[T])(implicit ME: MonadThrowable[F]): Flow[F, T] = {
    val result = fa.map(_.asRight[Throwable]).recoverWith { case e =>
      ME.pure(e.asLeft[T])
    }
    Flow.fromFRes(result)
  }

  def unit[F[_]: Monad]: Flow[F, Unit] = Flow.pure(())

  def log[F[_]: Monad](logF: F[Unit]): Flow[F, Unit] =
    EitherT(Monad[F].map(logF)(x => x.asRight[Throwable]))
}