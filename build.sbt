// *****************************************************************************
// Projects
// *****************************************************************************

lazy val xtream =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaActorTyped,
        library.akkaClusterTyped,
        library.akkaHttp,
        library.akkaStreamTyped,
        library.akkaHttp,
        library.pureConfig,
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka          = "2.6.3"
      val akkaHttp      = "10.1.8"
      val log4j         = "2.11.2"
      val log4jApiScala = "11.0"
      val pureConfig    = "0.12.2"
      val jsonHttp      = "1.31.0"
    }
    val akkaActorTyped   = "com.typesafe.akka"        %% "akka-actor-typed"   % Version.akka
    val akkaClusterTyped = "com.typesafe.akka"        %% "akka-cluster-typed" % Version.akka
    val akkaHttp         = "com.typesafe.akka"        %% "akka-http"          % Version.akkaHttp
    val akkaStreamTyped  = "com.typesafe.akka"        %% "akka-stream-typed"  % Version.akka
    val pureConfig       = "com.github.pureconfig"    %% "pureconfig"         % Version.pureConfig
    val httpJson         = "de.heikoseeberger"        %% "akka-http-json4s"   % Version.jsonHttp
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++ 
  scalafmtSettings ++
  commandAliases

lazy val commonSettings =
  Seq(
    scalaVersion := "2.13.1",
    organization := "me.hoffmann",
    organizationName := "Matheus Hoffmann",
    startYear := Some(2020),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8"//,
      //"-Ypartial-unification",
      //"-Ywarn-unused-import",
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true,
  )

lazy val commandAliases =
  addCommandAlias(
    "r1",
    """|reStart
       |---
       |-Dakka.cluster.seed-nodes.0=akka://xtream@127.0.0.1:25520
       |-Dakka.remote.artery.canonical.hostname=127.0.0.1
       |-Dxtream.api.hostname=127.0.0.1
       |-Dxtream.api.port=8080""".stripMargin
  ) ++
  addCommandAlias(
    "r2",
    """|reStart
       |---
       |-Dakka.cluster.seed-nodes.0=akka://xtream@127.0.0.1:25520
       |-Dakka.remote.artery.canonical.hostname=127.0.0.2
       |-Dxtream.api.hostname=127.0.0.2
       |-Dxtream.api.port=8080""".stripMargin
  )
