package dev.rmaiun.mabel.services

import cats.Applicative
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.mabel.errors.Errors.NoProcessorFound
import dev.rmaiun.mabel.postprocessor.{ AddPlayerPostProcessor, AddRoundPostProcessor, PostProcessor }
import dev.rmaiun.mabel.processors._
import dev.rmaiun.mabel.services.ProcessorStrategy.{ ADD_PLAYER_CMD, ADD_ROUND_CMD, ELO_RATING_CMD, SHORT_STATS_CMD }

case class ProcessorStrategy[F[_]: Applicative](
  addPlayerProcessor: AddPlayerProcessor[F],
  addRoundProcessor: AddRoundProcessor[F],
  seasonStatsProcessor: SeasonStatsProcessor[F],
  eloRatingProcessor: EloRatingProcessor[F],
  addRoundPostProcessor: AddRoundPostProcessor[F],
  addPlayerPostProcessor: AddPlayerPostProcessor[F]
) {
  def selectProcessor(cmd: String): Flow[F, Processor[F]] =
    cmd match {
      case ADD_PLAYER_CMD  => Flow.pure(addPlayerProcessor)
      case ADD_ROUND_CMD   => Flow.pure(addRoundProcessor)
      case SHORT_STATS_CMD => Flow.pure(seasonStatsProcessor)
      case ELO_RATING_CMD  => Flow.pure(eloRatingProcessor)
      case _               => Flow.error(NoProcessorFound(cmd))
    }

  def selectPostProcessor(cmd: String): Flow[F, List[PostProcessor[F]]] =
    cmd match {
      case ADD_PLAYER_CMD => Flow.pure(List(addPlayerPostProcessor))
      case ADD_ROUND_CMD  => Flow.pure(List(addRoundPostProcessor))
      case _              => Flow.pure(List.empty)
    }
}

object ProcessorStrategy {
  val ADD_PLAYER_CMD                                                       = "addPlayer"
  val ADD_ROUND_CMD                                                        = "addRound"
  val SHORT_STATS_CMD                                                      = "shortStats"
  val ELO_RATING_CMD                                                       = "eloRating"
  def apply[F[_]](implicit ev: ProcessorStrategy[F]): ProcessorStrategy[F] = ev
  def impl[F[_]: Applicative](
    addPlayerProcessor: AddPlayerProcessor[F],
    addRoundProcessor: AddRoundProcessor[F],
    seasonStatsProcessor: SeasonStatsProcessor[F],
    eloRatingProcessor: EloRatingProcessor[F],
    addRoundPostProcessor: AddRoundPostProcessor[F],
    addPlayerPostProcessor: AddPlayerPostProcessor[F]
  ): ProcessorStrategy[F] =
    new ProcessorStrategy[F](
      addPlayerProcessor,
      addRoundProcessor,
      seasonStatsProcessor,
      eloRatingProcessor,
      addRoundPostProcessor,
      addPlayerPostProcessor
    )
}
