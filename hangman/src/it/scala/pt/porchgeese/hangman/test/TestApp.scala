package pt.porchgeese.hangman.test

import cats.effect.{Clock, IO, Resource}
import doobie.Transactor
import pt.porchgeese.hangman.database.{GameRepository, GameWordRepository, GuessRepository, MatchupRepository, PlayerRepository}
import pt.porchgeese.hangman.domain.{WhiteSpaceCharacter, WordCharacter}
import pt.porchgeese.hangman.services.{GameService, GameWordService, GuessService, IdGeneratorService, MatchupService, PlayerService}
import pt.porchgeese.shared.config.{DatabaseConfig, ThreadPoolConfig}
import pt.porchgeese.shared.init
import pt.porchgeese.shared.test.docker.database
import pt.porchgeese.docker4s.Docker4SClient

object TestApp {
  case class TestApp(t: Transactor[IO], matchupService: MatchupService, playerService: PlayerService, gameService: GameService, gameWordService: GameWordService, guessService: GuessService)

  def withApp(testFunc: TestApp => IO[Any]): Unit =
    (for {
      ex <- init.fixedThreadPoolExecutor[IO](ThreadPoolConfig(10))
      cs        = IO.contextShift(ex)
      timer     = IO.timer(ex)
      conEffect = IO.ioConcurrentEffect(cs)
      config          <- Resource.liftF(init.config[IO])
      client          <- Docker4SClient.buildDockerResourceClient[IO]()(conEffect, timer)
      dbContainerPort <- database(client)(cs, timer)
      db <- init.hikaryTranscator[IO](
        DatabaseConfig(ThreadPoolConfig(1), "org.postgresql.Driver", s"jdbc:postgresql://127.0.0.1:${dbContainerPort.port}/test", "admin", "admin", Nil)
      )(cs, implicitly, implicitly)
      _ <- Resource.liftF(init.migrations[IO](config.databases.all.values.toList.flatMap(_.migrations).distinct, db))
      validWordCharacters = (('a' to 'z').toSet ++ ('A' to 'Z').toSet).map(WordCharacter)
      validWhitescpace    = Set(' ').map(WhiteSpaceCharacter)
      matchupRepository   = new MatchupRepository()
      playerRepository    = new PlayerRepository()
      gameRepository      = new GameRepository()
      guessRepository     = new GuessRepository()
      gameWordRepo        = new GameWordRepository()
      idGenerator         = new IdGeneratorService
      clock               = Clock.create[IO]
      app = TestApp(
        db,
        new MatchupService(matchupRepository, db, idGenerator, clock),
        new PlayerService(playerRepository, db, idGenerator, clock),
        new GameService(gameRepository, matchupRepository, db, idGenerator, clock),
        new GameWordService(gameRepository, gameWordRepo, matchupRepository, validWhitescpace, validWordCharacters, db, idGenerator, clock),
        new GuessService(gameRepository, matchupRepository, guessRepository, gameWordRepo, clock, db, idGenerator, validWordCharacters, validWhitescpace)
      )
    } yield app).use(app => testFunc(app)).void.unsafeRunSync()
}
