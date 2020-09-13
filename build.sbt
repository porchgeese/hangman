import Dependencies._
import sbt.Keys.libraryDependencies

import language.higherKinds
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val streaming = (project in file("streaming"))
  .settings(
    name := "streaming",
    libraryDependencies ++= (doobie ++ circe ++ cats ++ scalatest ++ logbackAndLog4s ++ fs2Kafka),
    scalacOptions ++= Seq(
      "-Xfatal-warnings"
    )
  )
  .dependsOn(common)

lazy val http = (project in file("http"))
  .settings(
    name := "http",
    libraryDependencies ++= (http4s ++ doobie ++ circe ++ cats ++ scalatest ++ logbackAndLog4s ++ fs2Kafka),
    scalacOptions ++= Seq(
      "-Xfatal-warnings"
    )
  )
  .dependsOn(common)

lazy val common = (project in file("common"))
  .settings(
    name := "common",
    libraryDependencies ++= (http4s ++ doobie ++ circe ++ cats ++ scalatest ++ logbackAndLog4s ++ fs2Kafka ++ pureConfig),
    scalacOptions ++= Seq(
      "-Xfatal-warnings"
    )
  )
