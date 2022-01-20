package dev.rmaiun.soos.helpers

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object ConfigProvider {
  val expectedCfg = "ARBITER_CFG"
  case class DbConfig(host: String, port: Int, database: String, username: String, password: String)

  case class ServerConfig(host: String, port: Int, tokens: String)

  case class AppConfig(
    privileged: String,
    archiveReceiver: String,
    expectedGames: Int,
    reportTimezone: String,
    topPlayersLimit: Int,
    minWritePermission: Int,
    defaultRole: String,
    defaultAlgorithm: String,
    startPoints: Int
  )

  case class ArchiveCfg(key: String, secret: String, token: String)

  case class Config(db: DbConfig, server: ServerConfig, app: AppConfig, archive: ArchiveCfg)

  def provideConfig(args: List[String] = Nil): Config =
    if (args.isEmpty || !args.exists(_.startsWith(expectedCfg))) {
      ConfigSource.default.loadOrThrow[Config]
    } else {
      val cfg = args.find(_.startsWith(expectedCfg)).fold("")(arg => arg.replace(s"$expectedCfg=", ""))
      ConfigSource.string(cfg).withFallback(ConfigSource.default).loadOrThrow[Config]
    }
}
