package dev.rmaiun.arbiter2

import cats.Monad
import cats.data.Kleisli
import cats.effect.kernel.Ref
import cats.effect.std.Dispatcher
import cats.effect.{ Async, Clock, Sync }
import cron4s.{ Cron, CronExpr }
import dev.profunktor.fs2rabbit.config.Fs2RabbitConfig
import dev.profunktor.fs2rabbit.config.declaration._
import dev.profunktor.fs2rabbit.effects.MessageEncoder
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.ExchangeType.Direct
import dev.profunktor.fs2rabbit.model._
import dev.rmaiun.arbiter2.bot.ArbiterBot
import dev.rmaiun.arbiter2.dtos.AmqpStructures
import dev.rmaiun.arbiter2.helpers.ConfigProvider.Config
import dev.rmaiun.arbiter2.helpers.{ ConfigProvider, DumpExporter }
import dev.rmaiun.flowtypes.FLog
import dev.rmaiun.flowtypes.Flow.MonadThrowable
import eu.timepit.fs2cron.Scheduler
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.nio.charset.Charset
import scala.collection.immutable.Queue
import scala.concurrent.duration.DurationInt

object Server {
  implicit def unsafeLogger[F[_]: Sync: Monad]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  def config(cfg: Config): Fs2RabbitConfig = Fs2RabbitConfig(
    virtualHost = cfg.broker.virtualHost,
    host = cfg.broker.host,
    port = cfg.broker.port,
    connectionTimeout = cfg.broker.timeout.seconds,
    username = Some(cfg.broker.username),
    password = Some(cfg.broker.password),
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

  private def initRabbitRoutes[F[_]: MonadThrowable](rc: RabbitClient[F]): F[Unit] = {
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

  def createRabbitConnection[F[_]: MonadThrowable](
    rc: RabbitClient[F]
  )(implicit F: Monad[F]): Stream[F, AmqpStructures[F]] = {
    implicit val stringMessageCodec: Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]] =
      Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]](s =>
        Monad[F].pure(s.copy(payload = s.payload.getBytes(Charset.defaultCharset())))
      )
    for {
      inPub                 <- publisher(inRK, rc)
      inPersistPub          <- publisher(inPersistRK, rc)
      outPub                <- publisher(outRK, rc)
      inConsumer            <- autoAckConsumer(inputQ, rc)
      inPersistenceConsumer <- autoAckConsumer(inputPersistenceQ, rc)
      outConsumer           <- autoAckConsumer(outputQ, rc)
    } yield AmqpStructures(inPub, inPersistPub, outPub, inPersistenceConsumer, inConsumer, outConsumer)
  }

  private def autoAckConsumer[F[_]: MonadThrowable](
    q: QueueName,
    rc: RabbitClient[F]
  ): Stream[F, Stream[F, AmqpEnvelope[String]]] =
    Stream
      .resource(rc.createConnectionChannel)
      .flatMap(implicit ch => Stream.eval(rc.createAutoAckConsumer(q)))

  private def publisher[F[_]: MonadThrowable](rk: RoutingKey, rc: RabbitClient[F])(implicit
    me: MessageEncoder[F, AmqpMessage[String]]
  ): Stream[F, AmqpMessage[String] => F[Unit]] =
    Stream
      .resource(rc.createConnectionChannel)
      .flatMap(implicit ch => Stream.eval(rc.createPublisher[AmqpMessage[String]](botExchange, rk)))

  def dumpCronEvaluation[F[_]: Async](exporter: DumpExporter[F])(implicit T: Clock[F]): Stream[F, Unit] = {
    val cronScheduler: Scheduler[F, CronExpr] = Cron4sScheduler.systemDefault[F]
    val cronTick                              = Cron.unsafeParse("0 0 20 ? * *")
    cronScheduler
      .awakeEvery(cronTick)
      .evalTap(_ => exporter.exportDump().value)
  }

  def stream[F[_]](implicit T: Clock[F], M: Monad[F], A: Async[F]): Stream[F, Nothing] = {
    implicit val cfg: Config = ConfigProvider.provideConfig
    for {
      dispatcher <- Stream.resource(Dispatcher[F])
      _          <- Stream.eval(FLog.info(cfg.toString).value)
      client     <- BlazeClientBuilder[F].withMaxWaitQueueLimit(1000).stream
      ref        <- Stream.eval(Ref[F].of(Queue[AmqpMessage[String]]()))
      rc         <- Stream.eval(RabbitClient[F](config(cfg), dispatcher))
      _          <- Stream.eval(initRabbitRoutes(rc))
      structs    <- createRabbitConnection(rc)
      bot        <- Stream.eval(A.pure(new ArbiterBot("1721059759:AAEyozK5-R3-7EN4yNz2jo-QvpOMoIF-iBI", structs)))
      module      = Program.initHttpApp(client, structs, ref)

      // With Middlewares in place
      finalHttpApp = org.http4s.server.middleware.Logger.httpApp(logHeaders = true, logBody = true)(module.httpApp)
      exitCode <-
        BlazeServerBuilder[F]
          .bindHttp(cfg.server.port, cfg.server.host)
          .withHttpApp(finalHttpApp)
          .serve
          .concurrently(structs.botInConsumer.flatMap(x => Stream.eval(module.queryHandler.process(x).value)))
          .concurrently(structs.botInPersistConsumer.flatMap(x => Stream.eval(module.persistHandler.process(x).value)))
          .concurrently(Stream.awakeDelay[F](4 hours).evalTap(_ => module.queryPublisher.run().value))
          .concurrently(Stream.awakeDelay[F](166 milliseconds).evalTap(_ => module.rlPublisher.safePublish().value))
//          .concurrently(dumpCronEvaluation(module.dumpExporter))
          .concurrently(Stream.eval(bot.startPolling()))
          .concurrently(structs.botOutConsumer.flatMap(x => Stream.eval(bot.processResponse(x))))
    } yield exitCode
  }.drain
}
