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
        library.kindProjector,
        library.logbackClassic,
        library.zLog,
        library.ciris,
        library.zio,
        library.zioWcats
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {}
    val zLog           = "dev.zio"        %% "zio-logging"       % "0.2.2"
    val ciris          = "is.cir"         %% "ciris"             % "1.0.4"
    val zio            = "dev.zio"        %% "zio"               % "1.0.0-RC17"
    val zioWcats       = "dev.zio"        %% "zio-interop-cats"  % "2.0.0.0-RC10"
    val kindProjector  = "org.typelevel"  %  "kind-projector"    % "0.11.0" cross CrossVersion.full
    val logbackClassic = "ch.qos.logback" %  "logback-classic"   % "1.2.3"

  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings //++
  //scalafmtSettings //++
  //commandAliases

lazy val commonSettings =
  Seq(
    scalaVersion := "2.13.1",
    organization := "me.hoffmann",
    organizationName := "Matheus Hoffmann",
    startYear := Some(2020),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
  )
