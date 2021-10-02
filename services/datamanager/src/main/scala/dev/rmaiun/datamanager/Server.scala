package dev.rmaiun.datamanager

import cats.Monad
import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import fs2.Stream
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

object Server {
  implicit def unsafeLogger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  val clientEC: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F], M: Monad[F]): Stream[F, Nothing] = {
    for {
      //general
      client <- BlazeClientBuilder[F](global).withMaxWaitQueueLimit(1000).stream
      httpApp = Module.initHttpApp(client)

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)
      exitCode <- BlazeServerBuilder[F](clientEC)
                    .bindHttp(9091, "0.0.0.0")
                    .withHttpApp(finalHttpApp)
                    .serve
    } yield exitCode
  }.drain
}
