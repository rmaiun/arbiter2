package dev.rmaiun.mabel.services

import cats.Applicative
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.errors.Errors.NoProcessorFound
import dev.rmaiun.mabel.processors._

case class ProcessorStrategy[F[_]: Applicative](
  addPlayerProcessor: AddPlayerProcessor[F],
  addRoundProcessor: AddRoundProcessor[F],
  seasonStatsProcessor: SeasonStatsProcessor[F],
  eloRatingProcessor: EloRatingProcessor[F]
) {
  def selectProcessor(cmd: String): Flow[F, Processor[F]] =
    cmd match {
      case "addPlayer"  => Flow.pure(addPlayerProcessor)
      case "addRound"   => Flow.pure(addRoundProcessor)
      case "shortStats" => Flow.pure(seasonStatsProcessor)
      case "eloRating"  => Flow.pure(eloRatingProcessor)
      case _            => Flow.error(NoProcessorFound(cmd))
    }
}

object ProcessorStrategy {
  def apply[F[_]](implicit ev: ProcessorStrategy[F]): ProcessorStrategy[F] = ev
  def impl[F[_]: Applicative](
    addPlayerProcessor: AddPlayerProcessor[F],
    addRoundProcessor: AddRoundProcessor[F],
    seasonStatsProcessor: SeasonStatsProcessor[F],
    eloRatingProcessor: EloRatingProcessor[F]
  ): ProcessorStrategy[F] =
    new ProcessorStrategy[F](addPlayerProcessor, addRoundProcessor, seasonStatsProcessor, eloRatingProcessor)
}
