package pt.porchgeese.hangman.test

import cats.effect.{Clock, IO, Resource}
import doobie.Transactor
import pt.porchgeese.hangman.database.MatchupRepository
import pt.porchgeese.hangman.services.{IdGeneratorService, MatchupService}
import pt.porchgeese.shared.config.{DatabaseConfig, ThreadPoolConfig}
import pt.porchgeese.shared.init
import pt.porchgeese.shared.test.docker.ContainerPort
import pt.porchgeese.shared.test.docker

object TestApp {
  case class TestApp(t: Transactor[IO], matchupService: MatchupService)

  def withApp(testFunc: TestApp => IO[Any]): Unit =
    (for {
      ex <- init.fixedThreadPoolExecutor[IO](ThreadPoolConfig(10))
      cs = IO.contextShift(ex)
      config       <- Resource.liftF(init.config[IO])
      dockerClient <- docker.dockerClient
      dbContainer  <- docker.database(dockerClient, 8080)
      db <- init.hikaryTranscator[IO](
        DatabaseConfig(ThreadPoolConfig(1), "org.postgresql.Driver", s"jdbc:postgresql://127.0.0.1:${dbContainer(ContainerPort(5432)).head.port}/test", "admin", "admin", Nil)
      )(cs, implicitly, implicitly)
      _ <- Resource.liftF(docker.dbHealthCheck(db))
      _ <- Resource.liftF(init.migrations[IO](config.databases.all.values.toList.flatMap(_.migrations).distinct, db))
      matchupRepository = new MatchupRepository()
      idGenerator       = new IdGeneratorService
      clock             = Clock.create[IO]
      app               = TestApp(db, new MatchupService(matchupRepository, db, idGenerator, clock))
    } yield app).use(app => testFunc(app)).unsafeRunSync()
}
