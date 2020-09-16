import sbt._

object Dependencies {
  lazy val CatsVersion       = "2.0.0"
  lazy val CatsEffectVersion = "2.2.0"
  lazy val ScalaTestVersion  = "3.2.0"
  lazy val Http4sVersion     = "0.21.7"
  lazy val CirceVersion      = "0.13.0"
  lazy val LogbackVersion    = "1.0.1"
  lazy val Log4CatsVersion   = "1.1.1"
  lazy val Log4sVersion      = "1.8.2"
  lazy val pureConfigVersion = "0.13.0"

  lazy val http4s = List(
    "org.http4s" %% "http4s-circe"        % Http4sVersion,
    "org.http4s" %% "http4s-dsl"          % Http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % Http4sVersion
  )

  lazy val circe = List(
    "io.circe" %% "circe-core"    % CirceVersion,
    "io.circe" %% "circe-generic" % CirceVersion,
    "io.circe" %% "circe-parser"  % CirceVersion
  )

  lazy val cats = List(
    "org.typelevel" %% "cats-core"   % CatsVersion,
    "org.typelevel" %% "cats-effect" % CatsEffectVersion
  )

  lazy val scalatest = List(
    "org.scalatest" %% "scalatest" % ScalaTestVersion % "test,it"
  )

  lazy val logbackAndLog4s = List(
    "org.log4s"         %% "log4s"           % Log4sVersion,
    "ch.qos.logback"     % "logback-classic" % LogbackVersion,
    "io.chrisdavenport" %% "log4cats-core"   % Log4CatsVersion
  )

  lazy val dockerTest = List(
    "com.github.docker-java" % "docker-java" % "3.2.5" % "test,it",
    "com.github.docker-java" % "docker-java-transport-httpclient5" % "3.2.5" % "test,it"
  )

  lazy val doobie = List(
    "org.tpolecat" %% "doobie-core"      % "0.9.0",
    "org.tpolecat" %% "doobie-hikari"    % "0.9.0",
    "org.tpolecat" %% "doobie-postgres"  % "0.9.0",
    "org.tpolecat" %% "doobie-scalatest" % "0.9.0" % "test,it"
  )

  lazy val fs2Kafka = List(
    "com.github.fd4s" %% "fs2-kafka" % "1.0.0"
  )

  lazy val pureConfig = List(
    "com.github.pureconfig" %% "pureconfig" % "0.13.0"
  )

  lazy val flyway = List(
    "org.flywaydb" % "flyway-core" % "5.1.4"
  )

}
