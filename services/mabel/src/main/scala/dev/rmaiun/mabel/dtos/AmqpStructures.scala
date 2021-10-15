package dev.rmaiun.mabel.dtos
import cats.effect.Concurrent
import dev.profunktor.fs2rabbit.model.{ AmqpEnvelope, AmqpMessage }
import dev.rmaiun.mabel.dtos.AmqpStructures.{ AmqpConsumer, AmqpPublisher }
import fs2.Stream

case class AmqpStructures[F[_]: Concurrent](
  botOutputPublisher: AmqpPublisher[F],
  botInputPersistenceConsumer: AmqpConsumer[F],
  botInputConsumer: AmqpConsumer[F]
)

object AmqpStructures {
  type AmqpPublisher[F[_]] = AmqpMessage[String] => F[Unit]
  type AmqpConsumer[F[_]]  = Stream[F, AmqpEnvelope[String]]
}
