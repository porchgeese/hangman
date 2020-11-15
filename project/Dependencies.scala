import sbt.Keys.resolvers
import sbt._


object Dependencies {

  val CatsVersion       = "2.0.0"
  val CatsEffectVersion = "2.2.0"
  val ScalaTestVersion  = "3.2.0"
  val Http4sVersion     = "0.21.7"
  val CirceVersion      = "0.13.0"
  val LogbackVersion    = "1.0.1"
  val Log4CatsVersion   = "1.1.1"
  val Log4sVersion      = "1.8.2"
  val pureConfigVersion = "0.13.0"
  val docke4sVersion    = "0.0.1-SNAPSHOT"

  val http4s = List(
    "org.http4s" %% "http4s-circe"        % Http4sVersion,
    "org.http4s" %% "http4s-dsl"          % Http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % Http4sVersion
  )

  val circe = List(
    "io.circe" %% "circe-core"    % CirceVersion,
    "io.circe" %% "circe-generic" % CirceVersion,
    "io.circe" %% "circe-parser"  % CirceVersion
  )

  val cats = List(
    "org.typelevel" %% "cats-core"   % CatsVersion,
    "org.typelevel" %% "cats-effect" % CatsEffectVersion
  )

  val scalatest = List(
    "org.scalatest" %% "scalatest" % ScalaTestVersion % "test,it"
  )

  val logbackAndLog4s = List(
    "org.log4s"         %% "log4s"           % Log4sVersion,
    "ch.qos.logback"     % "logback-classic" % LogbackVersion,
    "io.chrisdavenport" %% "log4cats-core"   % Log4CatsVersion
  )

  val dockerTest = List("pt.porchgeese" %% "docker4s-core" % docke4sVersion)

  val doobie = List(
    "org.tpolecat" %% "doobie-core"      % "0.9.0",
    "org.tpolecat" %% "doobie-hikari"    % "0.9.0",
    "org.tpolecat" %% "doobie-postgres"  % "0.9.0",
    "org.tpolecat" %% "doobie-scalatest" % "0.9.0" % "test,it"
  )

  val fs2Kafka = List(
    "com.github.fd4s" %% "fs2-kafka" % "1.0.0"
  )

  val pureConfig = List(
    "com.github.pureconfig" %% "pureconfig" % "0.13.0"
  )

  val flyway = List(
    "org.flywaydb" % "flyway-core" % "5.1.4"
  )

  val refined = List(
    "eu.timepit" %% "refined"            % "0.9.15",
    "eu.timepit" %% "refined-cats"       % "0.9.15",
    "eu.timepit" %% "refined-pureconfig" % "0.9.15"
  )

}
