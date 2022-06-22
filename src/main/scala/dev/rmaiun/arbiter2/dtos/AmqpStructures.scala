package dev.rmaiun.arbiter2.dtos

import dev.profunktor.fs2rabbit.model.{ AmqpEnvelope, AmqpMessage }
import dev.rmaiun.flowtypes.Flow.MonadThrowable
import AmqpStructures.{ AmqpConsumer, AmqpPublisher }
import fs2.Stream

case class AmqpStructures[F[_]: MonadThrowable](
  botInPublisher: AmqpPublisher[F],
  botOutPublisher: AmqpPublisher[F],
  botInPersistConsumer: AmqpConsumer[F],
  botInConsumer: AmqpConsumer[F]
)

object AmqpStructures {
  type AmqpPublisher[F[_]] = AmqpMessage[String] => F[Unit]
  type AmqpConsumer[F[_]]  = Stream[F, AmqpEnvelope[String]]
}
