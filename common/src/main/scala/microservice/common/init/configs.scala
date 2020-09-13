package microservice.common.init

import cats.implicits._
import cats.{Applicative, MonadError}
import microservice.common.init.utils.InitHashMap

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.matching.Regex

object configs {

  sealed trait InitError
  case class MissingConfigKey(key: String) extends InitError

  case class InitConfigs(
      httpClients: InitHashMap[HttpClientConfig],
      executors: InitHashMap[ThreadPoolConfig],
      databases: InitHashMap[DatabaseConfig],
      server: ServerConfig
  )


  case class HttpClientConfig(
      connectionTimeout: Duration,
      maxTotalConnections: Int,
      requestTimeout: Duration
  )
  case class DatabaseConfig(
      threadPool: ThreadPoolConfig,
      driver: String,
      url: String,
      user: String,
      password: String,
  )

  case class ServerConfig(
      threadPool: ThreadPoolConfig,
      idleTimeout: Duration,
      connectorPoolSize: Int,
      port: Int,
      host: String,
  )

  case class ConsumerConfig(
      bootstrapServers: String,
      groupId: String,
      batchCommitSize: Int,
      maxTimeCommitSize: FiniteDuration,
      concurrency: Int,
      topics: Regex
  )

  case class ThreadPoolConfig(parallelism: Int)

}
