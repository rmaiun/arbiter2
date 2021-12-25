package dev.rmaiun.soos

import cats.Monad
import cats.effect.{ ConcurrentEffect, ContextShift, Sync, Timer }
import cron4s.{ Cron, CronExpr }
import dev.rmaiun.soos.helpers.{ ConfigProvider, DumpExporter }
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import eu.timepit.fs2cron.{ ScheduledStreams, Scheduler }
import fs2.Stream
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ ExecutionContext, ExecutionContextExecutorService }

object Server {
  implicit def unsafeLogger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  val clientEC: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
  implicit val cfg: ConfigProvider.Config       = ConfigProvider.provideConfig

  def dumpCronEvaluation[F[_]: ConcurrentEffect](exporter: DumpExporter[F])(implicit
    T: Timer[F],
    C: ContextShift[F],
    M: Monad[F]
  ): Stream[F, Unit] = {
    val cronScheduler: Scheduler[F, CronExpr] = Cron4sScheduler.systemDefault[F]
    val cronTick                           = Cron.unsafeParse("0 0 20 ? * *")
    new ScheduledStreams(cronScheduler)
      .awakeEvery(cronTick)
      .evalTap(_ => exporter.exportDump().value)
  }

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F], M: Monad[F]): Stream[F, Nothing] = {
    for {
      //general
      client <- BlazeClientBuilder[F](global).withMaxWaitQueueLimit(1000).stream
      module  = Module.initHttpApp()
      // With Middlewares in place
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = false)(module.httpApp)
      exitCode <- BlazeServerBuilder[F](clientEC)
                    .bindHttp(cfg.server.port, cfg.server.host)
                    .withHttpApp(finalHttpApp)
                    .serve
                    .concurrently(dumpCronEvaluation(module.dumpExporter))
    } yield exitCode
  }.drain
}
