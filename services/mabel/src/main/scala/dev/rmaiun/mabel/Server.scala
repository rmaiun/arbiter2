package dev.rmaiun.mabel

import cats.Monad
import cats.data.Kleisli
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Sync, Timer}
import dev.profunktor.fs2rabbit.config.Fs2RabbitConfig
import dev.profunktor.fs2rabbit.config.declaration.DeclarationQueueConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.{AmqpMessage, ExchangeName, QueueName, RoutingKey}
import dev.rmaiun.mabel.dtos.AmqpStructures
import fs2.Stream
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import java.nio.charset.Charset
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

object Server {
  implicit def unsafeLogger[F[_]: Sync: Monad]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  val clientEC: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
  val blocker: Blocker =
    Blocker.liftExecutionContext(ExecutionContext.fromExecutorService(Executors.newCachedThreadPool()))

  val config: Fs2RabbitConfig = Fs2RabbitConfig(
    virtualHost = "arbiter",
    host = "127.0.0.1",
    port = 5672,
    connectionTimeout = 3,
    username = Some("rabbitmq"),
    password = Some("rabbitmq"),
    ssl = false,
    requeueOnNack = false,
    requeueOnReject = false,
    internalQueueSize = Some(500),
    automaticRecovery = true
  )

  private val requestCmdQ        = QueueName("input_q")
  private val requestPersistCmdQ = QueueName("request_persist_cmd")
  private val responseCmdQ       = QueueName("output_q")
  private val exchangeName       = ExchangeName("")
  private val routingKey         = RoutingKey("")

  def createRabbitConnection[F[_]: ConcurrentEffect: ContextShift: Monad](
    rc: RabbitClient[F]
  )(implicit F: Monad[F]): Stream[F, AmqpStructures[F]] = {
    implicit val stringMessageEncoder: Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]] =
      Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]](s =>
        Monad[F].pure(s.copy(payload = s.payload.getBytes(Charset.defaultCharset())))
      )

    val c1 = rc.createConnectionChannel
    val c2 = rc.createConnectionChannel
    val c3 = rc.createConnectionChannel
    for {
      x <- Stream.resource(c1).flatMap(implicit ch => Stream.eval(rc.createAutoAckConsumer(requestCmdQ)))
      y <- Stream.resource(c2).flatMap(implicit ch => Stream.eval(rc.createAutoAckConsumer(requestPersistCmdQ)))
      p <- Stream
             .resource(c3)
             .flatMap(implicit ch => Stream.eval(rc.createPublisher[AmqpMessage[String]](exchangeName, routingKey)))
    } yield AmqpStructures(p, x, y)
  }

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F], M: Monad[F]): Stream[F, Nothing] = {
    for {
      //general
      client     <- BlazeClientBuilder[F](global).withMaxWaitQueueLimit(1000).stream
      rc         <- Stream.eval(RabbitClient[F](config, blocker))
      structures <- createRabbitConnection(rc)
      httpApp     = Module.initHttpApp(client, structures)

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp._1)
      exitCode <- BlazeServerBuilder[F](clientEC)
                    .bindHttp(9091, "0.0.0.0")
                    .withHttpApp(finalHttpApp)
                    .serve
        .concurrently(structures.botInputConsumer.evalTap(x => httpApp._2.process(x).value))
    } yield exitCode
  }.drain
}
