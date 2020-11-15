package pt.porchgeese.hangman.validations

import cats.data.Validated.{Invalid, Valid}
import cats.data.{NonEmptyList, Validated}
import pt.porchgeese.hangman.domain.{GameCharacter, GameWord, WhiteSpaceCharacter, WordCharacter}
import cats.implicits._

object GameWordValidations {
  sealed trait GameWordValidations
  final case class InvalidCharacter(c: Char)     extends GameWordValidations
  final case object EndsWithWhitespace           extends GameWordValidations
  final case object BeginsWithWhitespace         extends GameWordValidations
  final case object HasMultiWhiteSpaceContiguous extends GameWordValidations
  final case object IsEmpty                      extends GameWordValidations

  def noMoreSubmissions(gws: List[GameWord]): Validated[Unit, Unit] = Validated.cond(gws.isEmpty, (), ())

  def endsWithLetter(word: NonEmptyList[GameCharacter]): Validated[GameWordValidations, Unit] =
    word.last match {
      case WhiteSpaceCharacter(_) => Invalid(EndsWithWhitespace)
      case WordCharacter(_)       => Valid(())
    }

  def startsWithLetter(word: NonEmptyList[GameCharacter]): Validated[GameWordValidations, Unit] =
    word.head match {
      case WhiteSpaceCharacter(_) => Invalid(BeginsWithWhitespace)
      case WordCharacter(_)       => Valid(())
    }
  def wordIsNotEmpty(c: List[Char]): Validated[GameWordValidations, NonEmptyList[Char]] = Validations.isNotEmpty(c).leftMap(_ => IsEmpty)

  def hasNoInvalidCharacters(allowedWordSymbols: Set[WordCharacter], allowedWhiteSpaceSymbols: Set[WhiteSpaceCharacter])(
      word: NonEmptyList[Char]
  ): Validated[NonEmptyList[GameWordValidations], NonEmptyList[GameCharacter]] =
    word
      .map[Either[Char, GameCharacter]] {
        case char if allowedWordSymbols.exists(_.c == char)       => WordCharacter(char).asRight
        case char if allowedWhiteSpaceSymbols.exists(_.c == char) => WhiteSpaceCharacter(char).asRight
        case other                                                => other.asLeft
      }
      .map(e => Validated.fromEither(e).leftMap[GameWordValidations](InvalidCharacter).toValidatedNel)
      .sequence

  def hasNoWhitespaceSequences(word: NonEmptyList[GameCharacter]): Validated[GameWordValidations, Unit] = {
    val result = word.toList.sliding(2).find {
      case WhiteSpaceCharacter(_) :: WhiteSpaceCharacter(_) :: _ => true
      case _                                                     => false
    }
    Validated.cond(result.isEmpty, (), HasMultiWhiteSpaceContiguous).void
  }
}
