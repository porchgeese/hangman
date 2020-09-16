import Dependencies._
import sbt.Keys.libraryDependencies
import sbt.addCompilerPlugin

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "pt.porchgeese"
ThisBuild / organizationName := "example"

parallelExecution in IntegrationTest := true

lazy val shared = (project in file("shared"))
  .settings(
    name := "shared",
    libraryDependencies ++= (http4s ++ doobie ++ circe ++ cats ++ scalatest ++ logbackAndLog4s ++ fs2Kafka ++ pureConfig ++ flyway ++ dockerTest),
    scalacOptions ++= Seq(
      "-Xfatal-warnings"
    ),
    buildInfoPackage := "pt.porchgeese.hangman",
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    Defaults.itSettings
  )
  .enablePlugins(BuildInfoPlugin)
  .configs(Test, IntegrationTest)

lazy val hangman = (project in file("hangman"))
  .settings(
    name := "hangman",
    libraryDependencies ++= (http4s ++ doobie ++ circe ++ cats ++ scalatest ++ logbackAndLog4s ++ fs2Kafka ++ pureConfig ++ flyway ++ dockerTest),
    scalacOptions ++= Seq(
      "-Xfatal-warnings"
    ),
    buildInfoPackage := "pt.porchgeese.hangman",
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    Defaults.itSettings
  )
  .enablePlugins(BuildInfoPlugin)
  .configs(Test, IntegrationTest)
  .dependsOn(shared)

lazy val streaming = (project in file("streaming"))
  .settings(
    name := "streaming",
    libraryDependencies ++= (doobie ++ circe ++ cats ++ scalatest ++ logbackAndLog4s ++ fs2Kafka),
    scalacOptions ++= Seq(
      "-Xfatal-warnings"
    ),
    Defaults.itSettings,
    buildInfoPackage := "pt.porchgeese.hangman",
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
  )
  .enablePlugins(BuildInfoPlugin)
  .configs(Test, IntegrationTest)
  .dependsOn(hangman)

lazy val http = (project in file("http"))
  .settings(
    name := "http",
    libraryDependencies ++= (http4s ++ doobie ++ circe ++ cats ++ scalatest ++ logbackAndLog4s ++ fs2Kafka),
    scalacOptions ++= Seq(
      "-Xfatal-warnings"
    ),
    buildInfoPackage := "pt.porchgeese.hangman",
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    Defaults.itSettings
  )
  .enablePlugins(BuildInfoPlugin)
  .configs(Test, IntegrationTest)
  .dependsOn(hangman)
