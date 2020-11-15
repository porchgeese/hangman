import Dependencies._
import sbt.Keys.libraryDependencies
import sbt.addCompilerPlugin

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "pt.porchgeese"
ThisBuild / organizationName := "example"
ThisBuild / resolvers += "scala4s snapshot repo" at "http://oss.sonatype.org/content/repositories/snapshots"

ThisBuild / scalacOptions := Seq(
  "-Xfatal-warnings",
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:adapted-args",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:deprecation",
  "-Xlint:doc-detached",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:package-object-classes",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Wdead-code",
  "-Wextra-implicit",
  "-Wnumeric-widen",
  "-Wunused:implicits",
  "-Wunused:imports",
  "-Wunused:locals",
  "-Wunused:params",
  "-Wunused:patvars",
  "-Wunused:privates",
  "-Wvalue-discard"
)
ThisBuild / parallelExecution in IntegrationTest := true

lazy val shared = (project in file("shared"))
  .settings(
    name := "shared",
    libraryDependencies ++= (http4s ++ doobie ++ circe ++ cats ++ scalatest ++ logbackAndLog4s ++ fs2Kafka ++ pureConfig ++ flyway),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    buildInfoPackage := "pt.porchgeese.hangman"
  )
  .configs(IntegrationTest)
  .enablePlugins(BuildInfoPlugin)

lazy val sharedTest = (project in file("sharedTest"))
  .settings(
    name := "shared-test",
    libraryDependencies ++= (http4s ++ doobie ++ circe ++ cats ++ scalatest ++ logbackAndLog4s ++ fs2Kafka ++ pureConfig ++ flyway ++ dockerTest),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    buildInfoPackage := "pt.porchgeese.hangman"
  )
  .configs(IntegrationTest)
  .enablePlugins(BuildInfoPlugin)

lazy val hangman = (project in file("hangman"))
  .configs(IntegrationTest)
  .settings(
    name := "hangman",
    libraryDependencies ++= (http4s ++ doobie ++ circe ++ cats ++ scalatest ++ logbackAndLog4s ++ fs2Kafka ++ pureConfig ++ flyway ++ refined),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    Defaults.itSettings
  )
  .configs(IntegrationTest)
  .dependsOn(shared, sharedTest % "compile->it")
  .enablePlugins(BuildInfoPlugin)

lazy val streaming = (project in file("streaming"))
  .configs(IntegrationTest)
  .settings(
    name := "hangman-streaming",
    libraryDependencies ++= (doobie ++ circe ++ cats ++ scalatest ++ logbackAndLog4s ++ fs2Kafka),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
  )
  .dependsOn(hangman)
  .enablePlugins(BuildInfoPlugin)

lazy val http = (project in file("http"))
  .settings(
    name := "hangman-http",
    libraryDependencies ++= (http4s ++ doobie ++ circe ++ cats ++ scalatest ++ logbackAndLog4s ++ fs2Kafka),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
  )
  .configs(IntegrationTest)
  .dependsOn(hangman)
  .enablePlugins(BuildInfoPlugin)
