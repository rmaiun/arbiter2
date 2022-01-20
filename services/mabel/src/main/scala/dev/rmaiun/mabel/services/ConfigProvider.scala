package dev.rmaiun.mabel.services

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object ConfigProvider {
  val expectedCfg = "ARBITER_CFG"

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

  def provideConfig(args: List[String] = Nil): Config =
    if (args.isEmpty || !args.exists(_.startsWith(expectedCfg))) {
      ConfigSource.default.loadOrThrow[Config]
    } else {
      val cfg = args.find(_.startsWith(expectedCfg)).fold("")(arg => arg.replace(s"$expectedCfg=", ""))
      ConfigSource.string(cfg).withFallback(ConfigSource.default).loadOrThrow[Config]
    }
}
