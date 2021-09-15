package dev.rmaiun.datamanager.helpers

import java.util.concurrent.Executors

import cats.effect.{Async, Blocker, ContextShift}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import dev.rmaiun.datamanager.helpers.ConfigProvider.Config
import doobie.hikari.HikariTransactor

import scala.concurrent.ExecutionContext

object TransactorProvider {

  def hikariTransactor[F[_] : Async : ContextShift](c: Config, useSSL:Boolean=false, allowPublicKeyRetrieval:Boolean = false): HikariTransactor[F] = {
    val config = new HikariConfig()
    val ec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
    val bec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(32))
    val sslParam = s"useSSL=$useSSL"
    val pcRetrievalParam = s"allowPublicKeyRetrieval=$allowPublicKeyRetrieval"
    val url = s"jdbc:mysql://${c.db.host}:${c.db.port}/${c.db.database}?$sslParam&$pcRetrievalParam&useUnicode=true&characterEncoding=UTF-8"
    config.setJdbcUrl(url)
    config.setUsername(c.db.username)
    config.setPassword(c.db.password)
    config.setMaximumPoolSize(5)
    config.setDriverClassName("com.mysql.cj.jdbc.Driver")
    HikariTransactor.apply[F](new HikariDataSource(config), ec, Blocker.liftExecutionContext(bec))
  }
}
