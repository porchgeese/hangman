package pt.porchgeese.hangman.services

import java.util.UUID

import cats.effect.IO
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pt.porchgeese.hangman.domain.{MatchupState, PlayerId}
import pt.porchgeese.hangman.services.MatchupService.MatchupFound
import pt.porchgeese.hangman.test.TestApp

class PlayerServiceTest extends AnyFreeSpec with Matchers {
  "The player service" - {
    "allows the creation of a player" in TestApp.withApp { app =>
      for {
        pId <- app.playerService.createPlayer("Player01")
      } yield pId.name shouldBe "Player01"
    }
  }
}
