package pt.porchgeese.hangman.validations

import cats.data.{NonEmptyList, Validated}
import pt.porchgeese.hangman.domain.{Matchup, PlayerId}

object Validations {
  def exits[A](option: Option[A]): Validated[Unit, A]               = Validated.fromOption(option, ())
  def isNotEmpty[A](c: List[A]): Validated[Unit, NonEmptyList[A]]   = Validated.fromOption(NonEmptyList.fromList(c), ())
  def playerMatches(p: PlayerId, m: Matchup): Validated[Unit, Unit] = Validated.cond(m.player == p || m.player2.contains(p), (), ())
  def matchupExists(m: Option[Matchup]): Validated[Unit, Matchup]   = Validations.exits(m)
}
