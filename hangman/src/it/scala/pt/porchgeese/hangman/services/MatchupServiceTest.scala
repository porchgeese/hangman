package pt.porchgeese.hangman.services

import java.util.UUID

import cats.effect.IO
import pt.porchgeese.hangman.domain.PlayerId
import pt.porchgeese.hangman.services.MatchupService.MatchupFound
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pt.porchgeese.hangman.domain.{MatchupState, PlayerId}
import pt.porchgeese.hangman.test.TestApp

class MatchupServiceTest extends AnyFreeSpec with Matchers {
  "The matchup service" - {
    "allows the creation of a matchup" in TestApp.withApp { app =>
      for {
        uuid <- IO.pure(UUID.randomUUID())
        mId  <- app.matchupService.createMatchup(PlayerId(uuid))
      } yield {
        mId.player shouldBe PlayerId(uuid)
        mId.player2 shouldBe None
        mId.state shouldBe MatchupState.Waiting
      }
    }

    "allows an existing matches to be matched" in TestApp.withApp { app =>
      for {
        player1     <- IO.pure(UUID.randomUUID())
        player2     <- IO.pure(UUID.randomUUID())
        mId         <- app.matchupService.createMatchup(PlayerId(player1))
        matchResult <- app.matchupService.matchupPlayer(PlayerId(player2))
      } yield matchResult shouldBe Right(MatchupFound(mId.id))
    }

    "if more than one matches are waiting, only one is matched against" in TestApp.withApp { app =>
      for {
        player1     <- IO.pure(UUID.randomUUID())
        player2     <- IO.pure(UUID.randomUUID())
        player3     <- IO.pure(UUID.randomUUID())
        mIdP1       <- app.matchupService.createMatchup(PlayerId(player1))
        _           <- app.matchupService.createMatchup(PlayerId(player2))
        matchResult <- app.matchupService.matchupPlayer(PlayerId(player3))
      } yield matchResult shouldBe Right(MatchupFound(mIdP1.id))
    }

    "if no matches are waiting, a new match is created" in TestApp.withApp { app =>
      for {
        player1 <- IO.pure(UUID.randomUUID())
        result  <- app.matchupService.matchupPlayer(PlayerId(player1))
      } yield {
        val newMatchup = result.left.getOrElse(fail("Result should be left")).matchup
        newMatchup.player shouldBe PlayerId(player1)
        newMatchup.player2 shouldBe None
        newMatchup.state shouldBe MatchupState.Waiting
      }
    }
  }
}
