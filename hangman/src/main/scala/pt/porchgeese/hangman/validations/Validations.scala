package pt.porchgeese.hangman.validations

import cats.data.{NonEmptyList, Validated}
import pt.porchgeese.hangman.domain.GameWord

object Validations {
  def exits[A](option: Option[A]): Validated[Unit, A]               = Validated.fromOption(option, ())
  def isNotEmpty[A](c: List[A]): Validated[Unit, NonEmptyList[A]]   = Validated.fromOption(NonEmptyList.fromList(c), ())

}
