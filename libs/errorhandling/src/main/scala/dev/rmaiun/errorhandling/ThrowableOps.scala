package dev.rmaiun.errorhandling

import cats.Monad
import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.either._

import java.sql.SQLException

object ThrowableOps {
  implicit class ThrowableErrorFormer[F[_]: Monad: Sync, T](fa: F[Either[Throwable, T]]) {
    def asFlowError: EitherT[F, Throwable, T] =
      EitherT(fa).leftFlatMap(err =>
        EitherT(Sync[F].delay(err.printStackTrace().asLeft[T])).leftFlatMap(_ => EitherT(makeBaseError(err)))
      )

    private def makeBaseError(err: Throwable): F[Either[Throwable, T]] =
      Monad[F].pure(new RuntimeException(err).asLeft[T])
  }

  implicit class SqlErrorFormer[F[_]: Monad, T](fa: F[Either[SQLException, T]]) {
    def adaptError: EitherT[F, Throwable, T] =
      EitherT(Monad[F].map(fa)(e => e.left.map(err => new RuntimeException(err.getMessage, err))))
  }
}
