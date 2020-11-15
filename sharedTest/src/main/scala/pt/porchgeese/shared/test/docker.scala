package pt.porchgeese.shared.test

import cats.data.Ior
import cats.effect.{Blocker, ContextShift, IO, Resource, Timer}
import doobie.util.transactor.Transactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import pt.porchgeese.docker4s.Docker4SClient.ResourceDocker4SClient
import pt.porchgeese.docker4s.implicits._
import pt.porchgeese.docker4s.domain
import pt.porchgeese.docker4s.domain.HealthStatus.HealthStatus
import pt.porchgeese.docker4s.domain.{ContainerDef, ContainerDetails, EnvVar, ExposedPort, HealthCheckConfig, HealthStatus, ImageName}
import pt.porchgeese.docker4s.healthcheck.HealthCheck

import scala.concurrent.duration._

object docker {

  def database(dockerCli: ResourceDocker4SClient[IO])(implicit cs: ContextShift[IO], t: Timer[IO]): Resource[IO, domain.ContainerPort] = {
    val image   = ImageName("postgres", "9.6-alpine")
    val envVars = List(EnvVar("POSTGRES_PASSWORD", "admin"), EnvVar("POSTGRES_USER", "admin"), EnvVar("POSTGRES_DB", "test"))
    val ports   = List(ExposedPort(5432))
    for {
      _           <- Resource.liftF(dockerCli.pullImage(image))
      containerId <- dockerCli.buildContainer(ContainerDef.simple(image, envVars, ports))
      _           <- dockerCli.startContainer(containerId)
      detailsOpt  <- Resource.liftF(dockerCli.getContainerDetails(containerId))
      details     <- Resource.liftF(detailsOpt.fold[IO[ContainerDetails]](IO.raiseError(new RuntimeException("Failed to start database container.")))(IO.pure))
      _           <- HealthCheck.evalRes[IO](dbHealthCheck, HealthCheckConfig(Ior.left(5.seconds)))
    } yield details.exposedPorts(ports.head).head
  }

  private def dbHealthCheck(implicit cs: ContextShift[IO]): IO[HealthStatus] = {
    val transactor = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/test",
      "admin",
      "admin",
      Blocker.liftExecutionContext(ExecutionContexts.synchronous)
    )
    sql"SELECT 1;".query[Int].option.transact(transactor).attempt
      .map(x => x.fold[HealthStatus](_ => HealthStatus.Unhealthy, x => x.fold[HealthStatus](HealthStatus.Unhealthy)(_ => HealthStatus.Healthy)))
  }

}
