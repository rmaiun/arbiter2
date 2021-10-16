package dev.rmaiun.mabel

import cats.Monad
import cats.data.Kleisli
import cats.effect.{ Blocker, ConcurrentEffect, ContextShift, Sync, Timer }
import dev.profunktor.fs2rabbit.config.Fs2RabbitConfig
import dev.profunktor.fs2rabbit.config.declaration._
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.ExchangeType.Direct
import dev.profunktor.fs2rabbit.model._
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
import scala.concurrent.{ ExecutionContext, ExecutionContextExecutorService }

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

  private val inputQ            = QueueName("input_q")
  private val inputPersistenceQ = QueueName("input_persistence_q")
  private val outputQ           = QueueName("output_q")
  private val botExchange       = ExchangeName("bot_exchange")
  private val inRK              = RoutingKey("bot_in_rk")
  private val inPersistRK       = RoutingKey("bot_in_p_rk")
  private val outRK             = RoutingKey("bot_out_rk")

  private def initRabbitRoutes[F[_]: ConcurrentEffect: ContextShift: Monad](rc: RabbitClient[F]): F[Unit] = {
    import cats.implicits._
    val channel = rc.createConnectionChannel
    channel.use { implicit ch =>
      for {
        _ <- rc.declareQueue(DeclarationQueueConfig(inputQ, Durable, NonExclusive, NonAutoDelete, Map()))
        _ <- rc.declareQueue(DeclarationQueueConfig(inputPersistenceQ, Durable, NonExclusive, NonAutoDelete, Map()))
        _ <- rc.declareQueue(DeclarationQueueConfig(outputQ, Durable, NonExclusive, NonAutoDelete, Map()))
        _ <-
          rc.declareExchange(DeclarationExchangeConfig(botExchange, Direct, Durable, NonAutoDelete, NonInternal, Map()))
        _ <- rc.bindQueue(inputQ, botExchange, inRK)
        _ <- rc.bindQueue(inputPersistenceQ, botExchange, inPersistRK)
        _ <- rc.bindQueue(outputQ, botExchange, outRK)
      } yield ()
    }
  }

  def createRabbitConnection[F[_]: ConcurrentEffect: ContextShift: Monad](
    rc: RabbitClient[F]
  )(implicit F: Monad[F]): Stream[F, AmqpStructures[F]] = {
    implicit val stringMessageEncoder: Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]] =
      Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]](s =>
        Monad[F].pure(s.copy(payload = s.payload.getBytes(Charset.defaultCharset())))
      )
    for {
      x <- Stream
             .resource(rc.createConnectionChannel)
             .flatMap(implicit ch => Stream.eval(rc.createAutoAckConsumer(inputQ)))
      y <- Stream
             .resource(rc.createConnectionChannel)
             .flatMap(implicit ch => Stream.eval(rc.createAutoAckConsumer(inputPersistenceQ)))
      p <- Stream
             .resource(rc.createConnectionChannel)
             .flatMap(implicit ch => Stream.eval(rc.createPublisher[AmqpMessage[String]](botExchange, outRK)))
    } yield AmqpStructures(p, y, x)
  }

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F], M: Monad[F]): Stream[F, Nothing] = {
    for {
      //general
      client     <- BlazeClientBuilder[F](global).withMaxWaitQueueLimit(1000).stream
      rc         <- Stream.eval(RabbitClient[F](config, blocker))
      _          <- Stream.eval(initRabbitRoutes(rc))
      structures <- createRabbitConnection(rc)
      httpApp     = Module.initHttpApp(client, structures)

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp._1)
      exitCode <- BlazeServerBuilder[F](clientEC)
                    .bindHttp(9092, "0.0.0.0")
                    .withHttpApp(finalHttpApp)
                    .serve
                    .concurrently(structures.botInputConsumer.flatMap(x => Stream.eval(httpApp._2.process(x).value)))
    } yield exitCode
  }.drain
}
