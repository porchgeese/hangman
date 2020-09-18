package pt.porchgeese.hangman.services

import java.util.UUID

import cats.data.NonEmptyList
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pt.porchgeese.hangman.domain.{GameState, MatchupId}
import pt.porchgeese.hangman.test.TestApp

class GameServiceTest extends AnyFreeSpec with Matchers {
  "The matchup service" - {
    "allow the creation of a game" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        player2 <- app.playerService.createPlayer("player02")
        matchup <- app.matchupService.createMatchup(player1.id)
        _       <- app.matchupService.matchupPlayer(player2.id)
        game    <- app.gameService.createGame(matchup.id)
      } yield {
        val gameResult = game.getOrElse(fail("expected either to be right"))
        gameResult.matchupId shouldBe matchup.id
        gameResult.state shouldBe GameState.Ready
      }
    }

    "reject the creation of a game which doesn't have enough players" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        matchup <- app.matchupService.createMatchup(player1.id)
        game    <- app.gameService.createGame(matchup.id)
      } yield {
        val error = game.left.getOrElse(fail("expected either to be left"))
        error shouldBe NonEmptyList(GameService.MatchupNotReady, List(GameService.WrongNumberOfPlayers))
      }
    }
    "reject the creation of a game which doesn't exist" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        _       <- app.matchupService.createMatchup(player1.id)
        game    <- app.gameService.createGame(MatchupId(UUID.randomUUID()))
      } yield {
        val error = game.left.getOrElse(fail("expected either to be left"))
        error shouldBe NonEmptyList(GameService.MatchupNotFound, Nil)
      }
    }
  }
}
