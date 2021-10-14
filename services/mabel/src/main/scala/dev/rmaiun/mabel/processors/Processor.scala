package dev.rmaiun.mabel.processors

import cats.{Applicative, Monad}
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.dtos.{BotRequest, ProcessorResponse}
import io.circe.{Decoder, Json}

trait Processor[F[_]] {
  def process(input: BotRequest): Flow[F, ProcessorResponse]

  protected def parseDto[T](body: Option[Json])(implicit d: Decoder[T], F: Applicative[F]): Flow[F, T] =
    body match {
      case Some(value) => Flow.fromEither(value.as[T])
      case None        => Flow.error(new RuntimeException("Bot request body is missed"))
    }
}
