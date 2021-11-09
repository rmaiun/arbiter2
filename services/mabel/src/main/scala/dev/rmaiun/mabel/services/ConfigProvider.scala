package dev.rmaiun.mabel.services

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object ConfigProvider {

  case class BrokerConfig(host: String, virtualHost: String, port: Int, username: String, password: String)

  case class ServerConfig(
    broker: BrokerConfig,
    mabelPath: String,
    mabelPathMock: String,
    notificationsEnabled: Boolean,
    host: String,
    port: Int,
    soosToken:String
  )

  def provideConfig: ServerConfig =
    ConfigSource.default.loadOrThrow[ServerConfig]
}
