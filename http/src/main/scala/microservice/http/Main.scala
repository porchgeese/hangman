package microservice.http

import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp, Resource, Sync, SyncIO}
import cats.implicits._
import microservice.common.init._
import microservice.common.init.configs.{InitError, MissingConfigKey}
import microservice.common.routes.{Health, HealthCheck}
import microservice.http.healthcheck.HealthCheckService
import microservice.http.init.{AppAndServices, externalDependencies}
import org.http4s.dsl.io._
import org.http4s.implicits._

import scala.concurrent.ExecutionContext

object Main extends IOApp.WithContext {

  override protected def executionContextResource: Resource[SyncIO, ExecutionContext] =
    Resource
      .make(SyncIO(Executors.newSingleThreadExecutor()))(tp => SyncIO(tp.shutdown()))
      .map(ExecutionContext.fromExecutorService)

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      externals      <- init.externalDependencies[IO]
      appAndServices <- init.services(externals)
      runningServer  <- server[IO](externals.config.server, appAndServices.routes)
    } yield runningServer).use(_ => IO.never)
}
