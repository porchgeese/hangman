package pt.porchgeese.hangman.validations

import cats.data.Validated
import pt.porchgeese.hangman.domain.{Matchup, MatchupState, PlayerId}

object MatchupValidations {
  def matchupHas2Players(m: Matchup): Validated[Unit, (PlayerId, PlayerId)] =
    Validated.fromOption(m.player2, ()).map(p => m.player -> p)
  def matchupIsReady(m: Matchup): Validated[Unit, Unit] =
    Validated.cond(m.state == MatchupState.Paired, (), ())
}
