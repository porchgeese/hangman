package microservice.http.healthcheck

import cats.effect.IO
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import microservice.common.init.utils.InitHashMap
import microservice.common.routes.{Health, Healthy, Unhealthy}

class HealthCheckService(dbConnections: InitHashMap[Transactor[IO]]) {

  def health: IO[Map[String, Map[String, Health]]] = {
    val dbHealth = dbConnections.all.toList
      .traverse {
        case (name, transactor) =>
          sql"SELECT 1;"
            .query[Unit]
            .to[List]
            .transact(transactor)
            .attempt
            .map[Health] {
              case Left(_)  => Unhealthy
              case Right(_) => Healthy
            }
            .map(name -> _)
      }
    dbHealth.map(dbs => Map("database" -> dbs.toMap))
  }

}
