package pt.porchgeese.hangman.http

import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp, Resource, SyncIO}
import pt.porchgeese.shared.init._

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
