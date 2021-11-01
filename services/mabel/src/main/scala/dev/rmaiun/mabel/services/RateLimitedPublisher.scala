package dev.rmaiun.mabel.services

import cats.syntax.foldable._
import dev.profunktor.fs2rabbit.model.AmqpMessage
import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.flowtypes.{ FLog, Flow }
import dev.rmaiun.mabel.Module.RateLimitQueue
import dev.rmaiun.mabel.dtos.AmqpStructures.AmqpPublisher
import io.chrisdavenport.log4cats.Logger

import scala.annotation.tailrec
import scala.collection.immutable.Queue

case class RateLimitedPublisher[F[_]: MonadThrowable: Logger](
  rateLimitQueue: RateLimitQueue[F],
  publisher: AmqpPublisher[F]
) {
  def safePublish(): Flow[F, Unit] =
    for {
      accessed <- Flow.effect(rateLimitQueue.access)
      msgData  <- Flow.pure(dequeueMessages(accessed._1))
      _        <- Flow.effect(rateLimitQueue.set(msgData._2))
      _        <- FLog.debug(s"Publishing to bot ${msgData._1.size} messages")
      _        <- publishMessages(msgData._1)
    } yield ()

  private def publishMessages(msgList: List[AmqpMessage[String]]): Flow[F, Unit] =
    msgList match {
      case Nil => Flow.unit
      case _   => msgList.map(m => Flow.effect(publisher(m))).sequence_
    }

  private def dequeueMessages(
    queue: Queue[AmqpMessage[String]]
  ): (List[AmqpMessage[String]], Queue[AmqpMessage[String]]) = {
    @tailrec
    def dequeueRecursively(
      queue: Queue[AmqpMessage[String]],
      elemsDequeue: Int,
      acc: List[AmqpMessage[String]]
    ): (List[AmqpMessage[String]], Queue[AmqpMessage[String]]) =
      if (queue.isEmpty || elemsDequeue == 0) {
        (acc, queue)
      } else {
        val dequeued = queue.dequeue
        dequeueRecursively(dequeued._2, elemsDequeue - 1, dequeued._1 :: acc)
      }
    dequeueRecursively(queue, 15, Nil)
  }
}

object RateLimitedPublisher {
  def apply[F[_]](implicit ev: RateLimitedPublisher[F]): RateLimitedPublisher[F] = ev
  def impl[F[_]: MonadThrowable: Logger](
    queue: RateLimitQueue[F],
    publisher: AmqpPublisher[F]
  ): RateLimitedPublisher[F] =
    new RateLimitedPublisher[F](queue, publisher)
}
