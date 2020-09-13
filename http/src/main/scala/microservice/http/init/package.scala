package microservice.http

import cats.MonadError
import cats.effect.{Async, ConcurrentEffect, ContextShift, IO, Resource, Sync, Timer}
import cats.implicits._
import cats.effect._
import microservice.common.init.{httpClient, _}
import microservice.common.init.configs.{InitConfigs, InitError, MissingConfigKey}
import microservice.common.init.utils.InitHashMap
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.server.Server
import org.http4s.implicits._
import microservice.common.routes.{Health, HealthCheck}
import microservice.http.healthcheck.HealthCheckService
import pureconfig.ConfigSource
import pureconfig.generic.auto._
package object init {
  case class ServiceConfig()

  case class AppAndServices[F[_]](
      external: Externals[F],
      routes: HttpApp[F]
  ) {
    private def copy(): Unit = ()
  }
  def serviceConfig[F[_]](implicit ME: MonadError[F,Throwable]): F[ServiceConfig] =
    ME.fromEither(
      ConfigSource.default.load[ServiceConfig]
        .leftMap(e => e.toList.map(e => e.origin.map(_.description() + " ").getOrElse("") + e.description).mkString("\n"))
        .leftMap(reason => new RuntimeException(reason))
    )

  def externalDependencies[F[_]: ContextShift: Sync: Async: ConcurrentEffect: Timer](implicit ME: MonadError[F, Throwable]): Resource[F, Externals[F]] =
    for {
      config <- Resource.liftF(config[F])

      httpConfigEC <- Resource.liftF(ME.fromEither(config.executors.getDefault.leftMap(handleInitError("httpEC"))))
      httpConfig   <- Resource.liftF(ME.fromEither((config.httpClients.getDefault.leftMap(handleInitError("httpConfig")))))
      httpEC       <- fixedThreadPoolExecutor[F](httpConfigEC)
      httpClient   <- httpClient[F](httpConfig, httpEC)

      dbConfg      <- Resource.liftF(ME.fromEither(config.databases.getDefault.leftMap(handleInitError("db"))))
      dbConnection <- hikaryTranscator[F](dbConfg)

      threadPoolConfig <- Resource.liftF(ME.fromEither(config.executors.getDefault.leftMap(handleInitError("threadPool"))))
      threadPool       <- fixedThreadPoolExecutor[F](threadPoolConfig)

    } yield Externals[F](InitHashMap.default(httpClient), InitHashMap.default(dbConnection), InitHashMap.default(threadPool), config)

  def handleInitError[A](p: String)(i: InitError): Throwable =
    i match {
      case MissingConfigKey(key) => new RuntimeException(s"Failed to find config ${key} within config ${p}")
    }

  def services(externals: Externals[IO])(implicit contextShift: ContextShift[IO], timer: Timer[IO]): Resource[IO, AppAndServices[IO]] =
    for {
      config             <- Resource.liftF[IO, ServiceConfig](serviceConfig[IO])
      healthCheckService <- Resource.pure[IO, HealthCheckService](new HealthCheckService(externals.dbConnections))
      healthCheckRoutes  <- Resource.pure[IO, HealthCheck[IO]](new HealthCheck[IO](healthCheckService.health))
      routes             <- Resource.pure[IO, HttpRoutes[IO]](healthCheckRoutes.routes)
    } yield AppAndServices[IO](externals, routes.orNotFound)

}