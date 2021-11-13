package dev.rmaiun.soos.helpers

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object ConfigProvider {

  case class DbConfig(host: String, port: Int, database: String, username: String, password: String)

  case class ServerConfig(host:String,port: Int, tokens:String)

  case class AppConfig(
    privileged: String,
    archiveReceiver: String,
    expectedGames: Int,
    reportTimezone: String,
    topPlayersLimit: Int,
    minWritePermission: Int,
    defaultRole: String,
    defaultAlgorithm:String,
    startPoints:Int
  )

  case class Config(db: DbConfig, server: ServerConfig, app: AppConfig)

  def provideConfig: Config =
    ConfigSource.default.loadOrThrow[Config]
}
