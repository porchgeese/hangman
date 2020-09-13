import sbt._

object Dependencies {
  lazy val CatsVersion       = "2.0.0"
  lazy val CatsEffectVersion = "2.2.0"
  lazy val ScalaTestVersion  = "3.2.0"
  lazy val Http4sVersion     = "0.21.7"
  lazy val CirceVersion      = "0.12.3"
  lazy val LogbackVersion    = "1.0.1"
  lazy val Log4sVersion      = "1.8.2"
  lazy val pureConfigVersion = "0.13.0"

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
    "org.scalatest" %% "scalatest" % ScalaTestVersion % Test
  )

  val logbackAndLog4s = List(
    "org.log4s"      %% "log4s"          % Log4sVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion
  )

  val doobie = List(
    "org.tpolecat" %% "doobie-core"      % "0.9.0",
    "org.tpolecat" %% "doobie-h2"        % "0.9.0" % Test,
    "org.tpolecat" %% "doobie-hikari"    % "0.9.0",
    "org.tpolecat" %% "doobie-postgres"  % "0.9.0",
    "org.tpolecat" %% "doobie-scalatest" % "0.9.0" % Test
  )

  val fs2Kafka = List(
    "com.github.fd4s" %% "fs2-kafka" % "1.0.0"
  )

  val pureConfig  = List(
    "com.github.pureconfig" %% "pureconfig" % "0.13.0"
  )

}
