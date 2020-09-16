package pt.porchgeese.shared

import java.util.concurrent.Executors

import cats.{ApplicativeError, Defer, MonadError}
import cats.effect._
import cats.implicits._
import doobie.Transactor
import doobie.hikari.HikariTransactor
import fs2.kafka.{ConsumerSettings, RecordDeserializer, _}
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.configuration.ClassicConfiguration
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import pt.porchgeese.shared.config.{ConsumerConfig, DatabaseConfig, HttpClientConfig, InitConfigs, ServerConfig, ThreadPoolConfig}
import pureconfig._
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext

object init {

  case class Externals[F[_]](
      httpClients: InitHashMap[Client[F]],
      dbConnections: InitHashMap[Transactor[F]],
      threadPools: InitHashMap[ExecutionContext],
      config: InitConfigs
  )

  def config[F[_]](implicit ME: MonadError[F, Throwable]): F[InitConfigs] =
    ME.fromEither(
      ConfigSource.default
        .load[InitConfigs]
        .leftMap(e => e.toList.map(e => e.origin.map(_.description() + " ").getOrElse("") + e.description).mkString("\n"))
        .leftMap(reason => new RuntimeException(reason))
    )

  def fixedThreadPoolExecutor[F[_]: Sync](config: ThreadPoolConfig): Resource[F, ExecutionContext] =
    Resource
      .make(Sync[F].delay(Executors.newFixedThreadPool(config.parallelism)))(tp => Sync[F].delay(tp.shutdown()))
      .map(ExecutionContext.fromExecutorService)

  def cachedThreadPoolExecutor[F[_]: Sync](config: ThreadPoolConfig): Resource[F, ExecutionContext] =
    Resource
      .make(Sync[F].delay(Executors.newFixedThreadPool(config.parallelism)))(tp => Sync[F].delay(tp.shutdown()))
      .map(ExecutionContext.fromExecutorService)

  def hikaryTranscator[F[_]: ContextShift: Async: Sync](db: DatabaseConfig): Resource[F, HikariTransactor[F]] =
    for {
      doobieCTP <- cachedThreadPoolExecutor[F](db.threadPool)
      doobieFTP <- fixedThreadPoolExecutor[F](db.threadPool)
      t <- HikariTransactor.newHikariTransactor[F](
        db.driver,
        db.url,
        db.user,
        db.password,
        doobieCTP,
        Blocker.liftExecutionContext(doobieFTP)
      )
    } yield t

  def httpClient[F[_]: ConcurrentEffect](config: HttpClientConfig, executionContext: ExecutionContext): Resource[F, Client[F]] =
    BlazeClientBuilder
      .apply(executionContext = executionContext)
      .withConnectTimeout(config.connectionTimeout)
      .withMaxTotalConnections(config.maxTotalConnections)
      .withRequestTimeout(config.requestTimeout)
      .resource

  def server[F[_]: Sync: ConcurrentEffect: Timer](server: ServerConfig, app: HttpApp[F]): Resource[F, Server[F]] =
    for {
      serverEC <- fixedThreadPoolExecutor[F](server.threadPool)
      server <- BlazeServerBuilder[F](serverEC)
        .withIdleTimeout(server.idleTimeout)
        .withConnectorPoolSize(server.connectorPoolSize)
        .withHttpApp(app)
        .bindHttp(server.port, server.host)
        .resource
    } yield server

  def migrations[F[_]: Sync](migrationLocations: List[String], t: HikariTransactor[F]): F[Unit] =
    Sync[F].delay {
      val config = new ClassicConfiguration()
      config.setLocations(migrationLocations.map(new Location(_)): _*)
      val fw = new Flyway(config)
      fw.setDataSource(t.kernel)
      fw.migrate()
    }.void

  def consumer[F[_]: Sync: ConcurrentEffect: ContextShift: Timer, K, V](
      consumerConfig: ConsumerConfig,
      handler: ConsumerRecord[K, V] => F[Unit]
  )(implicit k: RecordDeserializer[F, K], v: RecordDeserializer[F, V]): fs2.Stream[F, Unit] = {
    val settings = ConsumerSettings[F, K, V]
      .withBootstrapServers(consumerConfig.bootstrapServers)
      .withGroupId(consumerConfig.groupId)
    consumerStream[F]
      .using(settings)
      .evalTap(_.subscribe(consumerConfig.topics))
      .flatMap(_.stream)
      .mapAsync(consumerConfig.concurrency)(committable => handler(committable.record).as(committable.offset))
      .through(
        commitBatchWithin[F](
          consumerConfig.batchCommitSize,
          consumerConfig.maxTimeCommitSize
        )
      )
  }

  sealed trait InitError
  case class MissingConfigKey(key: String) extends InitError

}
