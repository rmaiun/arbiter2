package dev.rmaiun.mabel.services

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object ConfigProvider {

  case class BrokerCfg(host: String, virtualHost: String, port: Int, username: String, password: String, timeout: Int)
  case class IntegrationCfg(soos: ServiceCfg)
  case class ServiceCfg(path: String, stub: String, token: String)
  case class AppCfg(notifications: Boolean, reportTimezone: String)
  case class ServerCfg(host: String, port: Int)
  case class Config(
    broker: BrokerCfg,
    integration: IntegrationCfg,
    app: AppCfg,
    server: ServerCfg
  )

  def provideConfig: Config =
    ConfigSource.default.loadOrThrow[Config]
}
