name := "arbiter2"

version := "0.1"

scalaVersion := "2.13.2"

lazy val tftypes = (project in file("support/tftypes"))
  .settings(
    name := "tftypes",
    settings,
    libraryDependencies ++= tfTypesDependencies
  )
  .disablePlugins(AssemblyPlugin)

lazy val mongocore = (project in file("support/mongocore"))
  .settings(
    name := "mongocore",
    settings,
    libraryDependencies ++= mongoCoreDependencies
  )
  .dependsOn(tftypes)
  .disablePlugins(AssemblyPlugin)

lazy val dependencies =
  new {
    val Http4sVersion          = "0.21.5"
    val CirceVersion           = "0.13.0"
    val LogbackVersion         = "1.2.3"
    val MunitVersion           = "0.7.20"
    val MunitCatsEffectVersion = "0.13.0"
    val CatsCoreVersion        = "2.1.0"
    val CatsEffectVersion      = "2.3.1"
    val Mongo4CatsVersion      = "0.2.16"
    val Specs2Version          = "4.10.0"
    val ScalaTestVersion       = "3.2.2"

    val catsCore         = "org.typelevel"         %% "cats-core"           % CatsCoreVersion
    val catsEffect       = "org.typelevel"         %% "cats-effect"         % CatsEffectVersion
    val blazeServer      = "org.http4s"            %% "http4s-blaze-server" % Http4sVersion
    val blazeClient      = "org.http4s"            %% "http4s-blaze-client" % Http4sVersion
    val http4sCirce      = "org.http4s"            %% "http4s-circe"        % Http4sVersion
    val httpDsl          = "org.http4s"            %% "http4s-dsl"          % Http4sVersion
    val circeGeneric     = "io.circe"              %% "circe-generic"       % CirceVersion
    val circeParser      = "io.circe"              %% "circe-parser"        % CirceVersion
    val circeOptics      = "io.circe"              %% "circe-optics"        % CirceVersion
    val munit            = "org.scalameta"         %% "munit"               % MunitVersion           % Test
    val munitCE2         = "org.typelevel"         %% "munit-cats-effect-2" % MunitCatsEffectVersion % Test
    val logbackClassic   = "ch.qos.logback"         % "logback-classic"     % LogbackVersion
    val log4cats         = "io.chrisdavenport"     %% "log4cats-slf4j"      % "1.1.1"
    val svmSubs          = "org.scalameta"         %% "svm-subs"            % "20.2.0"
    val pureConfig       = "com.github.pureconfig" %% "pureconfig"          % "0.14.0"
    val mongoScalaDriver = "org.mongodb.scala"     %% "mongo-scala-driver"  % "4.3.0"
    val fs2Core          = "co.fs2"                %% "fs2-core"            % "2.4.4"
    val fs2IO            = "co.fs2"                %% "fs2-io"              % "2.4.2"
    val scalatest        = "org.scalatest"         %% "scalatest"           % ScalaTestVersion       % Test
    val spec2Core        = "org.specs2"            %% "specs2-core"         % Specs2Version          % Test
  }

lazy val tfTypesDependencies = Seq(
  dependencies.catsCore,
  dependencies.catsEffect,
  dependencies.log4cats
)

lazy val mongoCoreDependencies = Seq(
  dependencies.logbackClassic,
  dependencies.catsCore,
  dependencies.catsEffect,
  dependencies.mongoScalaDriver,
  dependencies.fs2Core,
  dependencies.scalatest,
  dependencies.spec2Core
)

lazy val settings = Seq(
  scalaVersion := "2.13.2",
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
