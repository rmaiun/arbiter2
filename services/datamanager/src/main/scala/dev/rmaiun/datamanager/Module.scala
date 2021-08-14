package dev.rmaiun.datamanager

import cats.Monad
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import dev.rmaiun.datamanager.helpers.{ConfigProvider, TransactorProvider}
import dev.rmaiun.datamanager.repositories.{AlgorithmRepo, RealmRepo}
import dev.rmaiun.datamanager.routes.DataManagerRoutes
import dev.rmaiun.datamanager.services.{AlgorithmService, RealmService}
import io.chrisdavenport.log4cats.Logger
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.client.Client
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.implicits._

object Module {
  def initHttpApp[F[_]: ConcurrentEffect: Monad: Logger](
    client: Client[F]
  )(implicit T: Timer[F], C: ContextShift[F]): HttpApp[F] = {
    lazy val cfg        = ConfigProvider.provideConfig
    lazy val transactor = TransactorProvider.hikariTransactor(cfg)

    lazy val algorithmRepo: AlgorithmRepo[F] = AlgorithmRepo.impl
    lazy val realmRepo                       = RealmRepo.impl

    lazy val algorithmService = AlgorithmService.impl(algorithmRepo, transactor)
    lazy val realmService     = RealmService.impl(realmRepo, algorithmService, transactor)

    // http
    val httpApp = DataManagerRoutes.algorithmRoutes[F](algorithmService)

    Router[F]("/algorithm" -> httpApp).orNotFound
  }
}
