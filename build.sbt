name := "arbiter2"
scalaVersion := "2.13.6"

lazy val arbiter2 = (project in file("."))
  .settings(
    name := "arbiter2",
    version := "2.2.6",
    settings,
    libraryDependencies ++= arbiterDependencies,
    Test / parallelExecution := false
  )
  .settings(
    flywayUrl := s"jdbc:mysql://${System.getProperty("fw.host", "localhost")}:3306/arbiter?allowPublicKeyRetrieval=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8",
    flywayUser := System.getProperty("fw.user", "root"),
    flywayPassword := System.getProperty("fw.pass", "rootpassword"),
    flywayLocations += System.getProperty("fw.locations", "db/test")
  )
  .settings(assemblySettings: _*)
  .dependsOn(common, tftypes, validation, errorHandling, protocol, serverAuth)
  .enablePlugins(FlywayPlugin)


lazy val assemblySettings = Seq(
  assembly / test := {},
  assembly / mainClass := Some("dev.rmaiun.arbiter2.Main"),
  assembly / assemblyJarName := "arbiter2.jar",
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x                             => MergeStrategy.first
  }
)

lazy val common = (project in file("libs/common"))
  .settings(
    name := "common",
    settings
  )
  .dependsOn(errorHandling)
  .disablePlugins(AssemblyPlugin)

lazy val serverAuth = (project in file("libs/serverauth"))
  .settings(
    name := "serverauth",
    settings,
    libraryDependencies ++= serverAuthDependencies
  )
  .dependsOn(errorHandling)
  .disablePlugins(AssemblyPlugin)

lazy val tftypes = (project in file("libs/flowtypes"))
  .settings(
    name := "flowtypes",
    settings,
    libraryDependencies ++= tfTypesDependencies
  )
  .dependsOn(errorHandling)
  .disablePlugins(AssemblyPlugin)

lazy val validation = (project in file("libs/validation"))
  .settings(
    name := "validation",
    settings,
    libraryDependencies ++= validationDependencies
  )
  .dependsOn(tftypes, errorHandling)
  .disablePlugins(AssemblyPlugin)

lazy val errorHandling = (project in file("libs/errorhandling"))
  .settings(
    name := "errorhandling",
    settings,
    libraryDependencies ++= errorHandlingDependencies
  )
  .disablePlugins(AssemblyPlugin)

lazy val protocol = (project in file("libs/protocol"))
  .settings(
    name := "protocol",
    settings,
    libraryDependencies ++= protocolDependencies
  )
  .disablePlugins(AssemblyPlugin)

lazy val dependencies =
  new {
    val Http4sVersion     = "0.23.10"
    val CirceVersion      = "0.14.1"
    val LogbackVersion    = "1.2.3"
    val CatsCoreVersion   = "2.7.0"
    val CatsEffectVersion = "3.3.5"
    val Specs2Version     = "4.16.0"
    val ScalaTestVersion  = "3.2.12"

    val catsCore       = "org.typelevel"               %% "cats-core"            % CatsCoreVersion
    val catsEffect     = "org.typelevel"               %% "cats-effect"          % CatsEffectVersion
    val blazeServer    = "org.http4s"                  %% "http4s-blaze-server"  % Http4sVersion
    val blazeClient    = "org.http4s"                  %% "http4s-blaze-client"  % Http4sVersion
    val http4sCirce    = "org.http4s"                  %% "http4s-circe"         % Http4sVersion
    val httpDsl        = "org.http4s"                  %% "http4s-dsl"           % Http4sVersion
    val circeGeneric   = "io.circe"                    %% "circe-generic"        % CirceVersion
    val circeParser    = "io.circe"                    %% "circe-parser"         % CirceVersion
    val circeOptics    = "io.circe"                    %% "circe-optics"         % CirceVersion
    val logbackClassic = "ch.qos.logback"               % "logback-classic"      % LogbackVersion
    val log4cats       = "org.typelevel"               %% "log4cats-slf4j"       % "2.2.0"
    val pureConfig     = "com.github.pureconfig"       %% "pureconfig"           % "0.14.0"
    val mysql          = "mysql"                        % "mysql-connector-java" % "8.0.11"
    val doobieCore     = "org.tpolecat"                %% "doobie-core"          % "1.0.0-M5"
    val doobieHikari   = "org.tpolecat"                %% "doobie-hikari"        % "1.0.0-M5"
    val accordCore     = "com.wix"                     %% "accord-core"          % "0.7.6"
    val dropboxSdk     = "com.dropbox.core"             % "dropbox-core-sdk"     % "5.1.0"
    val commons        = "commons-io"                   % "commons-io"           % "2.11.0"
    val fs2rabbit      = "dev.profunktor"              %% "fs2-rabbit"           % "4.1.1"
    val fs2cron        = "eu.timepit"                  %% "fs2-cron-cron4s"      % "0.7.1"
    val scaffeine      = "com.github.blemale"          %% "scaffeine"            % "5.1.2"
    val tapirHttp4s    = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"  % "0.20.0-M9"

    val bot4s      = "com.bot4s"                     %% "telegram-core"                  % "5.4.2" /*% Provided*/
    val sttpClient = "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % "3.6.2" /*% Provided*/

    val scalatest = "org.scalatest"       %% "scalatest"    % ScalaTestVersion % Test
    val spec2Core = "org.specs2"          %% "specs2-core"  % Specs2Version    % Test
    val mockito   = "org.scalatestplus"   %% "mockito-3-4"  % "3.2.10.0"       % Test
    val flexmark  = "com.vladsch.flexmark" % "flexmark-all" % "0.62.2"        % Test
  }

lazy val tfTypesDependencies = Seq(
  dependencies.catsCore,
  dependencies.catsEffect,
  dependencies.log4cats
)

lazy val serverAuthDependencies = Seq(
  dependencies.catsCore,
  dependencies.catsEffect,
  dependencies.blazeServer,
  dependencies.httpDsl,
  dependencies.circeGeneric,
  dependencies.http4sCirce
)

lazy val validationDependencies = Seq(
  dependencies.catsCore,
  dependencies.accordCore
)

lazy val errorHandlingDependencies = Seq(
  dependencies.catsCore,
  dependencies.catsEffect,
  dependencies.circeGeneric,
  dependencies.circeParser,
  dependencies.circeOptics
)

lazy val protocolDependencies = Seq(
  dependencies.circeGeneric,
  dependencies.circeParser,
  dependencies.circeOptics
)

lazy val arbiterDependencies = Seq(
  dependencies.blazeServer,
  dependencies.blazeClient,
  dependencies.http4sCirce,
  dependencies.httpDsl,
  dependencies.circeGeneric,
  dependencies.circeParser,
  dependencies.circeOptics,
  dependencies.logbackClassic,
  dependencies.log4cats,
  dependencies.pureConfig,
  dependencies.mysql,
  dependencies.doobieCore,
  dependencies.doobieHikari,
  dependencies.fs2rabbit,
  dependencies.scaffeine,
  dependencies.tapirHttp4s,
  dependencies.mockito,
  dependencies.dropboxSdk,
  dependencies.commons,
  dependencies.fs2cron,
  dependencies.bot4s,
  dependencies.sttpClient,
  dependencies.scalatest,
  dependencies.spec2Core,
  dependencies.flexmark
)

lazy val settings = Seq(
  scalaVersion := "2.13.6",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-feature"
    //      "-Wconf:any:error"
    //      "-Xfatal-warnings",
    //      "-Ywarn-unused-imports"
  ),
  addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
  addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
  testFrameworks += new TestFramework("munit.Framework")
)

lazy val formatAll = taskKey[Unit]("Run scala formatter for all projects")

formatAll := {
  (arbiter2 / Compile / scalafmt).value
  (arbiter2 / Test / scalafmt).value
}

Test / testOptions ++= Seq(
  Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
  Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports")
)
