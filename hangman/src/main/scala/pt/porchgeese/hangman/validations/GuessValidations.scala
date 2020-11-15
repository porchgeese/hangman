package pt.porchgeese.hangman.validations

import cats.data.Validated
import pt.porchgeese.hangman.domain.{GameWord, Guess, WhiteSpaceCharacter, WordCharacter}

object GuessValidations {

  sealed trait GuessError
  final case object WhitespaceIsNotAValidGuess extends GuessError
  final case object GuessIsNotUnique           extends GuessError
  final case object InvalidChar                extends GuessError
  final case object WordIsComplete             extends GuessError

  def guessIsUnique(g: List[Guess], c: Char): Validated[GuessError, Unit] =
    Validated.cond(!g.exists(_.letter == c), (), GuessIsNotUnique)

  def guessIsValid(allowedWordSymbols: Set[WordCharacter], alloweWS: Set[WhiteSpaceCharacter])(c: Char): Validated[GuessError, Unit] = {
    val isValidSymbol = allowedWordSymbols.exists(_.c == c)
    val isWhiteSpace  = alloweWS.exists(_.c == c)
    Validated
      .cond(!isWhiteSpace, (), WhitespaceIsNotAValidGuess)
      .andThen(_ => Validated.cond(isValidSymbol, (), InvalidChar))
  }

  def wordIsComplete(guesses: List[Guess], gameWord: GameWord): Validated[GuessError, Unit] = {
    val unguessedLetters = gameWord.word.toList.distinct.diff(guesses.map(_.letter))
    Validated.cond(unguessedLetters.nonEmpty, (), WordIsComplete)
  }
}
