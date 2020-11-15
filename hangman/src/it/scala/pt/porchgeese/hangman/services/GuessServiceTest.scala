package pt.porchgeese.hangman.services

import cats.data.NonEmptyList
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pt.porchgeese.hangman.test.TestApp
import pt.porchgeese.hangman.validations.GuessValidations

class GuessServiceTest extends AnyFreeSpec with Matchers {
  "The guess service" - {
    "allows adding a valid guess to an existing game" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        player2 <- app.playerService.createPlayer("player02")
        matchup <- app.matchupService.createMatchup(player1.id)
        _       <- app.matchupService.matchupPlayer(player2.id)
        game    <- app.gameService.createGame(matchup.id).map(_.getOrElse(fail("game not created")))
        _       <- app.gameWordService.addGameWord(player1.id, game.id, "a word")
        _       <- app.gameWordService.addGameWord(player2.id, game.id, "another word")
        guess   <- app.guessService.addGuess('c', player1.id, game.id)
      } yield {
        val result = guess.getOrElse(fail("guess should be created"))
        result.letter shouldBe 'c'
        result.gameId shouldBe game.id
        result.player shouldBe player1.id
      }
    }

    "rejects adding a guess if the word was already fully guessed" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        player2 <- app.playerService.createPlayer("player02")
        matchup <- app.matchupService.createMatchup(player1.id)
        _       <- app.matchupService.matchupPlayer(player2.id)
        game    <- app.gameService.createGame(matchup.id).map(_.getOrElse(fail("game not created")))
        _       <- app.gameWordService.addGameWord(player1.id, game.id, "cccc")
        _       <- app.gameWordService.addGameWord(player2.id, game.id, "another word")
        guess01 <- app.guessService.addGuess('c', player2.id, game.id)
        guess02 <- app.guessService.addGuess('d', player2.id, game.id)
      } yield {
        guess01.getOrElse(fail("guess should be created"))
        val result = guess02.left.getOrElse(fail("guess should be created"))
        result shouldBe GuessService.InvalidGuess(NonEmptyList.one(GuessValidations.WordIsComplete))
      }
    }

    "rejects adding a guess if the guess is a whitespace char" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        player2 <- app.playerService.createPlayer("player02")
        matchup <- app.matchupService.createMatchup(player1.id)
        _       <- app.matchupService.matchupPlayer(player2.id)
        game    <- app.gameService.createGame(matchup.id).map(_.getOrElse(fail("game not created")))
        _       <- app.gameWordService.addGameWord(player1.id, game.id, "cccc")
        _       <- app.gameWordService.addGameWord(player2.id, game.id, "another word")
        guess   <- app.guessService.addGuess(' ', player2.id, game.id)
      } yield {
        val result = guess.left.getOrElse(fail("guess should not be created"))
        result shouldBe GuessService.InvalidGuess(NonEmptyList.one(GuessValidations.WhitespaceIsNotAValidGuess))
      }
    }

    "rejects adding a guess if the guess has already been submitted" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        player2 <- app.playerService.createPlayer("player02")
        matchup <- app.matchupService.createMatchup(player1.id)
        _       <- app.matchupService.matchupPlayer(player2.id)
        game    <- app.gameService.createGame(matchup.id).map(_.getOrElse(fail("game not created")))
        _       <- app.gameWordService.addGameWord(player1.id, game.id, "a word")
        _       <- app.gameWordService.addGameWord(player2.id, game.id, "another word")
        _       <- app.guessService.addGuess('c', player2.id, game.id)
        guess   <- app.guessService.addGuess('c', player2.id, game.id)
      } yield {
        val result = guess.left.getOrElse(fail("guess should not be created"))
        result shouldBe GuessService.InvalidGuess(NonEmptyList.one(GuessValidations.GuessIsNotUnique))
      }
    }

    "rejects adding a guess with invalid chars" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        player2 <- app.playerService.createPlayer("player02")
        matchup <- app.matchupService.createMatchup(player1.id)
        _       <- app.matchupService.matchupPlayer(player2.id)
        game    <- app.gameService.createGame(matchup.id).map(_.getOrElse(fail("game not created")))
        _       <- app.gameWordService.addGameWord(player1.id, game.id, "a word")
        _       <- app.gameWordService.addGameWord(player2.id, game.id, "another word")
        guess   <- app.guessService.addGuess('@', player2.id, game.id)
      } yield {
        val result = guess.left.getOrElse(fail("guess should not be created"))
        result shouldBe GuessService.InvalidGuess(NonEmptyList.one(GuessValidations.InvalidChar))
      }
    }
  }
}
