// *****************************************************************************
// Projects
// *****************************************************************************

lazy val zex =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.logbackClassic,
        library.zLog,
        library.sl4j,
        library.zLogSl4j,
        library.janino,
        library.zio,
        library.zioCats,
        library.appium,
        library.akka,
        "eu.timepit" %% "refined-cats"            % "0.9.13",
        library.catsRetry
      ) ++
      library.Ciris
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      //Logging
      val ZLog    = "0.2.2"
      val Sl4j    = "1.7.30"
      val Janino  = "3.1.0"
      val LogBack = "1.2.3"

      //ZIO
      val ZIO        = "1.0.0-RC18-2"
      val ZIOInterop = "2.0.0.0-RC12"

      //Misc
      val Appium        = "7.3.0"
      val Akka          = "2.6.4"
      val Retry         = "1.1.0"
//      val BetterMonadic = "0.3.1"
//      val Projector     = "0.11.0"

      //Config
      val Ciris  = "1.0.4"

    }

    val zLog           = "dev.zio"             %% "zio-logging"         % Version.ZLog
    val zLogSl4j       = "dev.zio"             %% "zio-logging-slf4j"   % Version.ZLog
    val sl4j           = "org.slf4j"            % "slf4j-api"           % Version.Sl4j
    val janino         = "org.codehaus.janino"  % "janino"              % Version.Janino
    val logbackClassic = "ch.qos.logback"       %  "logback-classic"    % Version.LogBack

    val zio            = "dev.zio"             %% "zio"                 % Version.ZIO
    val zioCats        = "dev.zio"             %% "zio-interop-cats"    % Version.ZIOInterop

    val appium         = "io.appium"            % "java-client"         % Version.Appium
    val akka           = "com.typesafe.akka"   %% "akka-actor-typed"    % Version.Akka
    val catsRetry      = "com.github.cb372"    %% "cats-retry"          % Version.Retry


    val Ciris = Seq(
      "is.cir" %% "ciris",
      "is.cir" %% "ciris-enumeratum",
      "is.cir" %% "ciris-refined"
    ).map(_ % Version.Ciris)

  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings = commonSettings

lazy val commonSettings =
  Seq(
    scalaVersion := "2.13.1",
    organization := "me.hoffmann",
    organizationName := "Matheus Hoffmann",
    startYear := Some(2020),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
  )
