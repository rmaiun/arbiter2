package dev.rmaiun.mabel.services

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object ConfigProvider {

  case class BrokerConfig(host: String, virtualHost: String, port: Int, username: String, password: String)

  case class ServerConfig(broker: BrokerConfig, mabelPath: String, mabelPathMock: String, host: String, port: Int)

  def provideConfig: ServerConfig =
    ConfigSource.default.loadOrThrow[ServerConfig]
}
