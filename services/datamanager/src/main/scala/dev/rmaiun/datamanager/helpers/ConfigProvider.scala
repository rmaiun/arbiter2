package dev.rmaiun.datamanager.helpers

import pureconfig.ConfigSource

object ConfigProvider {

  case class AppConfig(topPlayersLimit: Int,
                       minGames: Int,
                       winPoints: Int,
                       winShutoutPoints: Int,
                       losePoints: Int,
                       loseShutoutPoints: Int)

  case class DbConfig(host: String,
                      port: Int,
                      database: String,
                      username: String,
                      password: String)

  case class ServerConfig(port: Int)

  case class Config(app: AppConfig,
                    db: DbConfig,
                    server: ServerConfig)

  def provideConfig: Config = {
    ConfigSource.default.loadOrThrow[Config]
  }
}


