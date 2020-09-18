package pt.porchgeese.hangman.services

import cats.Applicative
import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.effect.{Async, Clock, IO}
import cats.implicits._
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import pt.porchgeese.hangman.database.GameWordRepository.GameWordFilter
import pt.porchgeese.hangman.database.{GameRepository, GameWordRepository, MatchupRepository}
import pt.porchgeese.hangman.domain.{Game, GameCharacter, GameId, GameWord, GameWordId, Matchup, PlayerId, WhiteSpaceCharacter, WordCharacter}
import pt.porchgeese.hangman.services.GameWordService.GameWordServiceError
import pt.porchgeese.hangman.validations.GameWordValidations.GameWordValidations
import pt.porchgeese.hangman.validations.{GameWordValidations, MatchupValidations, Validations}

import scala.concurrent.duration._

class GameWordService(
    gameRepo: GameRepository,
    gameWordRepo: GameWordRepository,
    matchupRepo: MatchupRepository,
    validWS: Set[WhiteSpaceCharacter],
    validWC: Set[WordCharacter],
    db: Transactor[IO],
    idGenerator: IdGeneratorService,
    clock: Clock[IO]
) {
  def addGameWord(player: PlayerId, gameId: GameId, word: String): IO[Either[NonEmptyList[GameWordServiceError], GameWord]] =
    (for {
      id   <- Async[ConnectionIO].liftIO(idGenerator.generateId.map(GameWordId(_)))
      now  <- Async[ConnectionIO].liftIO(clock.realTime(MICROSECONDS))
      game <- gameRepo.findGame(gameId)
      matchup <- game match {
        case Some(game) => matchupRepo.findMatchup(game.matchupId)
        case None       => Applicative[ConnectionIO].pure(None)
      }
      gameWordFilter = GameWordFilter().copy(player = player.some, game = gameId.some)
      alreadyDefinedWords <- gameWordRepo.gameWordsWithFilter(gameWordFilter)
      gameWord =
        GameWordService
          .validateWord(validWC, validWS)(word)
          .map(_ => GameWord.newGameWord(id, gameId, player, word, now))
          .toValidatedNel
          .andThen(gameWord => GameWordService.validateWordAdding(gameWord, game, matchup, alreadyDefinedWords))
          .toEither
      result <- gameWord match {
        case Left(errors) => Applicative[ConnectionIO].pure(errors.asLeft[GameWord])
        case Right(gw)    => gameWordRepo.createGameWord(gw).as(gw.asRight[NonEmptyList[GameWordServiceError]])
      }
    } yield result).transact(db)
}

object GameWordService {
  sealed trait GameWordServiceError
  final case object GameDoesNotExist                                      extends GameWordServiceError
  final case object MatchupDoesNotExist                                   extends GameWordServiceError
  final case object PlayerDoesNotMatch                                    extends GameWordServiceError
  final case object OnlyOneGameWordPerGame                                extends GameWordServiceError
  final case class InvalidWord(reason: NonEmptyList[GameWordValidations]) extends GameWordServiceError

  def validateWord(allowedWC: Set[WordCharacter], allowedWS: Set[WhiteSpaceCharacter])(word: String): Validated[InvalidWord, NonEmptyList[GameCharacter]] =
    GameWordValidations
      .wordIsNotEmpty(word.toList)
      .leftMap(e => InvalidWord(NonEmptyList.one(e)))
      .andThen(nonEmpty =>
        GameWordValidations
          .hasNoInvalidCharacters(allowedWC, allowedWS)(nonEmpty)
          .leftMap(InvalidWord)
      )
      .andThen { validWord =>
        (
          GameWordValidations.endsWithLetter(validWord).toValidatedNel,
          GameWordValidations.startsWithLetter(validWord).toValidatedNel,
          GameWordValidations.hasNoWhitespaceSequences(validWord).toValidatedNel
        ).mapN((_, _, _) => validWord)
          .leftMap(InvalidWord)
      }

  def validateWordAdding(
      gameWord: GameWord,
      game: Option[Game],
      matchup: Option[Matchup],
      gameWords: List[GameWord]
  ): ValidatedNel[GameWordServiceError, GameWord] = {
    val gameExists    = Validations.exits(game).leftMap(_ => GameDoesNotExist).toValidatedNel
    val matchupExists = Validations.exits(matchup).leftMap(_ => MatchupDoesNotExist).toValidatedNel

    (gameExists, matchupExists)
      .mapN((g, m) => (g, m))
      .andThen {
        case (_, matchup) =>
          (
            MatchupValidations.playerMatches(gameWord.playerId, matchup).leftMap(_ => PlayerDoesNotMatch).toValidatedNel,
            GameWordValidations.noMoreSubmissions(gameWords).leftMap(_ => OnlyOneGameWordPerGame).toValidatedNel
          ).mapN((_, _) => gameWord)
      }
  }

}
