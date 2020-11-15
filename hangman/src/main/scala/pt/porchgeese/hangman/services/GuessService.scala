package pt.porchgeese.hangman.services

import cats.Applicative
import cats.data.{NonEmptyList, Validated}
import cats.effect.{Async, Clock, IO}
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pt.porchgeese.hangman.database.GameWordRepository.GameWordFilter
import pt.porchgeese.hangman.database.GuessRepository.GuessFilter
import pt.porchgeese.hangman.database.{GameRepository, GameWordRepository, GuessRepository, MatchupRepository}
import pt.porchgeese.hangman.domain.{GameId, GameWord, Guess, GuessId, Matchup, PlayerId, WhiteSpaceCharacter, WordCharacter}
import pt.porchgeese.hangman.services.GuessService.GuessServiceErrors
import pt.porchgeese.hangman.validations.{GuessValidations, Validations}
import pt.porchgeese.hangman.validations.GuessValidations.GuessError

import scala.concurrent.duration._

class GuessService(
    gameRepository: GameRepository,
    matchupRepository: MatchupRepository,
    guessRepository: GuessRepository,
    gameWordRepository: GameWordRepository,
    clock: Clock[IO],
    db: Transactor[IO],
    idGenerator: IdGeneratorService,
    validWC: Set[WordCharacter],
    validWS: Set[WhiteSpaceCharacter]
) {
  def addGuess(letter: Char, player: PlayerId, gameId: GameId): IO[Either[GuessServiceErrors, Guess]] =
    (for {
      game <- gameRepository.findGame(gameId)
      matchup <- game match {
        case Some(game) => matchupRepository.findMatchup(game.matchupId)
        case None       => Applicative[ConnectionIO].pure(None)
      }
      guesses <- guessRepository.findGuesses(GuessFilter(playerId = Some(player), gameId = Some(gameId)))
      opponentPlayer = matchup match {
        case Some(m) => (m.player :: m.player2.toList).filterNot(_ == player).headOption
        case _       => None
      }
      gameWord <- opponentPlayer match {
        case Some(opponent) => gameWordRepository.gameWordsWithFilter(GameWordFilter(game = Some(gameId), player = Some(opponent))).map(_.headOption)
        case None           => Applicative[ConnectionIO].pure(None)
      }
      now <- Async[ConnectionIO].liftIO(clock.realTime(MILLISECONDS))
      id  <- Async[ConnectionIO].liftIO(idGenerator.generateId.map(GuessId(_)))
      guess       = Guess(id, letter, gameId, player, now)
      validations = GuessService.validateNewGuess(validWC, validWS)(matchup, guesses, letter, player, gameWord).toEither
      result <- validations match {
        case Left(errors) => Applicative[ConnectionIO].pure(errors.asLeft[Guess])
        case Right(_)     => guessRepository.createGuess(guess).as(guess.asRight[GuessServiceErrors])
      }
    } yield result).transact(db)
}

object GuessService {
  sealed trait GuessServiceErrors
  final case class InvalidGuess(error: NonEmptyList[GuessError]) extends GuessServiceErrors
  final case object UndefinedMatchup                             extends GuessServiceErrors
  final case object UndefinedGameWord                            extends GuessServiceErrors
  final case object PlayerDoesNotMatch                           extends GuessServiceErrors

  def validateNewGuess(
      validWC: Set[WordCharacter],
      validWS: Set[WhiteSpaceCharacter]
  )(m: Option[Matchup], guesses: List[Guess], guess: Char, p: PlayerId, word: Option[GameWord]): Validated[GuessServiceErrors, Unit] =
    Validations
      .matchupExists(m)
      .leftMap(_ => UndefinedMatchup)
      .andThen { matchup =>
        Validations.playerMatches(p, matchup).leftMap(_ => PlayerDoesNotMatch)
      }
      .andThen(_ => Validations.exits(word).leftMap(_ => UndefinedGameWord))
      .andThen { gameWord =>
        (
          GuessValidations.guessIsUnique(guesses, guess).toValidatedNel,
          GuessValidations.guessIsValid(validWC, validWS)(guess).toValidatedNel,
          GuessValidations.wordIsComplete(guesses, gameWord).toValidatedNel
        ).mapN((_, _, _) => ()).leftMap[GuessServiceErrors](InvalidGuess)
      }
}
