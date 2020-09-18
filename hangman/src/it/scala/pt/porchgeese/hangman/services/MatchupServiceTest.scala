package pt.porchgeese.hangman.services

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pt.porchgeese.hangman.domain.MatchupState
import pt.porchgeese.hangman.services.MatchupService.MatchupFound
import pt.porchgeese.hangman.test.TestApp

class MatchupServiceTest extends AnyFreeSpec with Matchers {
  "The matchup service" - {
    "allows the creation of a matchup" in TestApp.withApp { app =>
      for {
        player  <- app.playerService.createPlayer("player01")
        matchup <- app.matchupService.createMatchup(player.id)
      } yield {
        matchup.player shouldBe player.id
        matchup.player2 shouldBe None
        matchup.state shouldBe MatchupState.Waiting
      }
    }

    "allows an existing matches to be matched" in TestApp.withApp { app =>
      for {
        player1     <- app.playerService.createPlayer("player01")
        player2     <- app.playerService.createPlayer("player02")
        mId         <- app.matchupService.createMatchup(player1.id)
        matchResult <- app.matchupService.matchupPlayer(player2.id)
      } yield matchResult shouldBe Right(MatchupFound(mId.id))
    }

    "if more than one matches are waiting, only one is matched against" in TestApp.withApp { app =>
      for {
        player1     <- app.playerService.createPlayer("player01")
        player2     <- app.playerService.createPlayer("player02")
        player3     <- app.playerService.createPlayer("player03")
        mIdP1       <- app.matchupService.createMatchup(player1.id)
        _           <- app.matchupService.createMatchup(player2.id)
        matchResult <- app.matchupService.matchupPlayer(player3.id)
      } yield matchResult shouldBe Right(MatchupFound(mIdP1.id))
    }

    "if no matches are waiting, a new match is created" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        result  <- app.matchupService.matchupPlayer(player1.id)
      } yield {
        val newMatchup = result.left.getOrElse(fail("Result should be left")).matchup
        newMatchup.player shouldBe player1.id
        newMatchup.player2 shouldBe None
        newMatchup.state shouldBe MatchupState.Waiting
      }
    }
  }
}
