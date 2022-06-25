package dev.rmaiun.arbiter2.helpers

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object ConfigProvider {

  case class DbConfig(host: String, port: Int, database: String, username: String, password: String)
  case class BrokerConfig(
    host: String,
    virtualHost: String,
    port: Int,
    username: String,
    password: String,
    timeout: Int
  )

  case class AppConfig(
    notifications: Boolean,
    reportTimezone: String,
    privileged: Long,
    archiveReceiver: String,
    expectedGames: Int,
    topPlayersLimit: Int,
    minWritePermission: Int,
    defaultRole: String,
    defaultAlgorithm: String,
    startPoints: Int
  )
  case class ArchivingConfig(enabled: Boolean, key: String, secret: String, token: String)
  case class ServerConfig(host: String, port: Int, tokens: String)
  case class BotConfig(token: String, version: String)
  case class Config(
    db: DbConfig,
    broker: BrokerConfig,
    app: AppConfig,
    archiving: ArchivingConfig,
    server: ServerConfig,
    bot: BotConfig
  )

  def provideConfig: Config =
    ConfigSource.default.loadOrThrow[Config]
}
