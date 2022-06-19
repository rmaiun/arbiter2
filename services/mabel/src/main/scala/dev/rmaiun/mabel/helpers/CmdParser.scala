package dev.rmaiun.mabel.helpers

import cats.Applicative
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import io.circe.{ Decoder, Json }

trait CmdParser[F[_]] {
  protected def parseDto[T](body: Option[Json])(implicit d: Decoder[T], F: Applicative[F]): Flow[F, T] =
    body match {
      case Some(value) => Flow.fromEither(value.as[T])
      case None        => Flow.error(new RuntimeException("Bot request body is missed"))
    }
}
