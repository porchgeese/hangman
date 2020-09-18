package pt.porchgeese.hangman.services

import cats.data.{NonEmptyList, ValidatedNel}
import cats.effect.{Clock, IO}
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import pt.porchgeese.hangman.database.{GameRepository, MatchupRepository}
import pt.porchgeese.hangman.domain.{Game, GameId, Matchup, MatchupId}
import pt.porchgeese.hangman.services.GameService.GameServiceError
import pt.porchgeese.hangman.validations.{MatchupValidations, Validations}

import scala.concurrent.duration._

class GameService(
    gameRepo: GameRepository,
    matchupRepo: MatchupRepository,
    db: Transactor[IO],
    idGenerator: IdGeneratorService,
    clock: Clock[IO]
) {
  def createGame(matchupId: MatchupId): IO[Either[NonEmptyList[GameServiceError], Game]] =
    for {
      matchup <- matchupRepo.findMatchup(matchupId).transact(db)
      gameId  <- idGenerator.generateId.map(GameId(_))
      now     <- clock.realTime(MILLISECONDS)
      game       = Game.newMatchup(gameId, matchupId, now)
      validation = GameService.validateGameCreation(matchup).toEither
      result <- validation match {
        case Left(e)  => IO.pure(e.asLeft)
        case Right(_) => gameRepo.creatGame(game).transact(db).as(game.asRight)
      }
    } yield result
}

object GameService {
  sealed trait GameServiceError
  case object MatchupNotFound      extends GameServiceError
  case object MatchupNotReady      extends GameServiceError
  case object WrongNumberOfPlayers extends GameServiceError

  def validateGameCreation(m: Option[Matchup]): ValidatedNel[GameServiceError, Unit] =
    Validations
      .exits(m)
      .leftMap(_ => MatchupNotFound)
      .toValidatedNel
      .andThen((m: Matchup) =>
        (
          MatchupValidations.matchupIsReady(m).leftMap(_ => MatchupNotReady).toValidatedNel,
          MatchupValidations.matchupHas2Players(m).leftMap(_ => WrongNumberOfPlayers).toValidatedNel
        ).mapN((_, _) => ())
      )
}
