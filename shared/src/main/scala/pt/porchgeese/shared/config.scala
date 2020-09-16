package pt.porchgeese.shared

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.matching.Regex

object config {
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
      migrations: List[String]
  )

  case class ServerConfig(
      threadPool: ThreadPoolConfig,
      idleTimeout: Duration,
      connectorPoolSize: Int,
      port: Int,
      host: String
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

  case class InitConfigs(
      httpClients: InitHashMap[HttpClientConfig],
      executors: InitHashMap[ThreadPoolConfig],
      databases: InitHashMap[DatabaseConfig],
      server: ServerConfig
  )
}
