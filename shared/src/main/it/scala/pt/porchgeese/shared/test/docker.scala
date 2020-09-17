package pt.porchgeese.shared.test

import java.io.Closeable

import cats.effect.{IO, Resource}
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import com.github.dockerjava.transport.DockerHttpClient
import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.model.{HostConfig, ExposedPort => ClientExposedPort}
import com.github.dockerjava.core.DockerClientImpl
import doobie.util.transactor.Transactor
import doobie.implicits._
import pt.porchgeese.shared.Foo

import scala.jdk.CollectionConverters._
object docker {
  case class ContainerId(value: String) extends AnyVal
  case class ContainerPort(port: Int)
  case class ExposedPort(port: Int)
  def dockerClient: Resource[IO, DockerClient] =
    Resource
      .make(IO.delay {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder.build
        val client = new ApacheDockerHttpClient.Builder()
          .dockerHost(config.getDockerHost)
          .sslConfig(config.getSSLConfig)
          .build()
        config -> client
      }) {
        case (_, client) => IO.delay(client.close())
      }
      .flatMap {
        case (config, httpClient) =>
          Resource.make(IO.delay(DockerClientImpl.getInstance(config, httpClient)))(client => IO.delay(client.close))
      }

  def database(dockerCli: DockerClient, port: Long): Resource[IO, Map[ContainerPort, List[ExposedPort]]] =
    startContainer(dockerCli)(
      "postgres:9.6-alpine",
      List("POSTGRES_PASSWORD=admin", "POSTGRES_USER=admin", "POSTGRES_DB=test"),
      List(5432)
    )

  def dbHealthCheck(t: Transactor[IO]): IO[Unit] =
    fs2.Stream
      .repeatEval(
        sql"SELECT 1".query[Unit].unique.transact(t).attempt
      )
      .takeWhile(_.isLeft)
      .compile
      .drain

  def startContainer(dockerCli: DockerClient)(
      image: String,
      envVariables: List[String],
      exposedPorts: List[Int]
  ): Resource[IO, Map[ContainerPort, List[ExposedPort]]] = {
    def removeContainer(id: ContainerId): Unit = {
      dockerCli.killContainerCmd(id.value).exec()
      dockerCli.removeContainerCmd(id.value).exec()
    }
    Resource
      .make(
        IO.delay {
          val result = dockerCli.pullImageCmd(image).exec(new PullImageResultCallback())
          result.awaitCompletion()
          val container = dockerCli
            .createContainerCmd(image)
            .withEnv(envVariables: _*)
            .withExposedPorts(exposedPorts.map(p => new ClientExposedPort(p)): _*)
            .withHostConfig(new HostConfig().withPublishAllPorts(true))
            .exec()
          val containerId  = ContainerId(container.getId)
          val shutdownHook = new Thread(() => removeContainer(containerId))
          dockerCli.startContainerCmd(containerId.value).exec()
          val containerDetails = dockerCli.inspectContainerCmd(containerId.value).exec()
          val portMappings = containerDetails.getNetworkSettings.getPorts.getBindings.asScala.toList.map {
            case (key, value) => key.getPort -> value.toList.map(_.getHostPortSpec.toInt) //TODO: should probably not do a toInt here
          }.toMap
          Runtime.getRuntime.addShutdownHook(shutdownHook)
          (shutdownHook, containerId, portMappings)
        }
      ) {
        case (hook, container, _) =>
          IO.delay {
            removeContainer(container)
            Runtime.getRuntime.removeShutdownHook(hook)
          }
      }
      .map(result => result._3.toList.map { case (k, v) => ContainerPort(k) -> v.map(ExposedPort) }.toMap)
  }
}
