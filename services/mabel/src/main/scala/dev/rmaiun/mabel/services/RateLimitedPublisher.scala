package dev.rmaiun.mabel.services

import cats.effect.Sync
import cats.syntax.apply._
import cats.syntax.foldable._
import dev.profunktor.fs2rabbit.model.AmqpMessage
import dev.rmaiun.flowtypes.Flow.{Flow, MonadThrowable}
import dev.rmaiun.flowtypes.{FLog, Flow}
import dev.rmaiun.mabel.Program.RateLimitQueue
import dev.rmaiun.mabel.dtos.AmqpStructures.AmqpPublisher
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.annotation.tailrec
import scala.collection.immutable.Queue

case class RateLimitedPublisher[F[_]: MonadThrowable: Sync](
  rateLimitQueue: RateLimitQueue[F],
  publisher: AmqpPublisher[F]
) {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)

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
      case _ =>
        msgList.map(m => FLog.info(s"Sending message to BOT: ${m.payload}") *> Flow.effect(publisher(m))).sequence_
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
    dequeueRecursively(queue, 5, Nil)
  }
}

object RateLimitedPublisher {
  def apply[F[_]](implicit ev: RateLimitedPublisher[F]): RateLimitedPublisher[F] = ev
  def impl[F[_]: MonadThrowable: Sync](
    queue: RateLimitQueue[F],
    publisher: AmqpPublisher[F]
  ): RateLimitedPublisher[F] =
    new RateLimitedPublisher[F](queue, publisher)
}
