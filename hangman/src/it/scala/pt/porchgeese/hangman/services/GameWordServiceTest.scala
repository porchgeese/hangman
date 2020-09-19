package pt.porchgeese.hangman.services

import java.util.UUID

import cats.data.NonEmptyList
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pt.porchgeese.hangman.domain.GameId
import pt.porchgeese.hangman.services.GameWordService.InvalidWord
import pt.porchgeese.hangman.test.TestApp
import pt.porchgeese.hangman.validations.GameWordValidations
import pt.porchgeese.hangman.validations.GameWordValidations.HasMultiWhiteSpaceContiguous
class GameWordServiceTest extends AnyFreeSpec with Matchers {
  "The gameword service" - {
    "allows adding a valid word to an existing game" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        player2 <- app.playerService.createPlayer("player02")
        matchup <- app.matchupService.createMatchup(player1.id)
        _       <- app.matchupService.matchupPlayer(player2.id)
        game    <- app.gameService.createGame(matchup.id).map(_.getOrElse(fail("game not created")))
        word    <- app.gameWordService.addGameWord(player1.id, game.id, "aValidWord")
      } yield {
        val validWord = word.getOrElse(fail("word should successfully be added"))
        validWord.word shouldBe "aValidWord"
        validWord.playerId shouldBe player1.id
        validWord.gameId shouldBe game.id
      }
    }

    "fail if a word addition is attempted to a non existing game" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        player2 <- app.playerService.createPlayer("player02")
        matchup <- app.matchupService.createMatchup(player1.id)
        _       <- app.matchupService.matchupPlayer(player2.id)
        _       <- app.gameService.createGame(matchup.id).map(_.getOrElse(fail("game not created")))
        word    <- app.gameWordService.addGameWord(player1.id, GameId(UUID.randomUUID()), "aValidWord")
      } yield {
        val error = word.left.getOrElse(fail("word should not have been created"))
        error shouldBe NonEmptyList.of(GameWordService.GameDoesNotExist, GameWordService.MatchupDoesNotExist)
      }
    }

    "fail if a word addition is attempted twice" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        player2 <- app.playerService.createPlayer("player02")
        matchup <- app.matchupService.createMatchup(player1.id)
        _       <- app.matchupService.matchupPlayer(player2.id)
        game    <- app.gameService.createGame(matchup.id).map(_.getOrElse(fail("game not created")))
        _       <- app.gameWordService.addGameWord(player1.id, game.id, "aValidWord")
        word    <- app.gameWordService.addGameWord(player1.id, game.id, "aValidWord")
      } yield {
        val error = word.left.getOrElse(fail("word should not have been created"))
        error shouldBe NonEmptyList.of(GameWordService.OnlyOneGameWordPerGame)
      }
    }

    "fail if a word addition is attempted to a game which the player is not a part of" in TestApp.withApp { app =>
      for {
        player1 <- app.playerService.createPlayer("player01")
        player2 <- app.playerService.createPlayer("player02")
        player3 <- app.playerService.createPlayer("player03")

        matchupP1P2 <- app.matchupService.createMatchup(player1.id)
        _           <- app.matchupService.matchupPlayer(player2.id)

        matchupP1P3 <- app.matchupService.createMatchup(player1.id)
        _           <- app.matchupService.matchupPlayer(player2.id)

        gameP1P2 <- app.gameService.createGame(matchupP1P2.id).map(_.getOrElse(fail("game not created")))
        _        <- app.gameService.createGame(matchupP1P3.id).map(_.getOrElse(fail("game not created")))

        word <- app.gameWordService.addGameWord(player3.id, gameP1P2.id, "aValidWord")
      } yield {
        val error = word.left.getOrElse(fail("word should not have been created"))
        error shouldBe NonEmptyList(GameWordService.PlayerDoesNotMatch, Nil)
      }
    }

    "fail if the word provided is not valid" in TestApp.withApp { app =>
      for {
        player1            <- app.playerService.createPlayer("player01")
        player2            <- app.playerService.createPlayer("player02")
        matchup            <- app.matchupService.createMatchup(player1.id)
        _                  <- app.matchupService.matchupPlayer(player2.id)
        game               <- app.gameService.createGame(matchup.id).map(_.getOrElse(fail("game not created")))
        whiteSpaceEnd      <- app.gameWordService.addGameWord(player1.id, game.id, "whiteSpaceEnd ")
        whiteSpaceBegining <- app.gameWordService.addGameWord(player1.id, game.id, " whiteSpaceBegin")
        whiteSpaceBoth     <- app.gameWordService.addGameWord(player1.id, game.id, " whiteSpace ")
        invalidCharacters  <- app.gameWordService.addGameWord(player1.id, game.id, "white$pace")
        multipleWhiteSpace <- app.gameWordService.addGameWord(player1.id, game.id, "whites  pace")
      } yield {
        val whiteSpaceEndErr = whiteSpaceEnd.left.getOrElse(fail("should not accept invalid words"))
        whiteSpaceEndErr shouldBe NonEmptyList.of(InvalidWord(NonEmptyList.of(GameWordValidations.EndsWithWhitespace)))
        val whiteSpaceBeginingErr = whiteSpaceBegining.left.getOrElse(fail("should not accept invalid words"))
        whiteSpaceBeginingErr shouldBe NonEmptyList.of(InvalidWord(NonEmptyList.of(GameWordValidations.BeginsWithWhitespace)))
        val whiteSpaceBothErr = whiteSpaceBoth.left.getOrElse(fail("should not accept invalid words"))
        whiteSpaceBothErr shouldBe NonEmptyList.of(InvalidWord(NonEmptyList.of(GameWordValidations.EndsWithWhitespace, GameWordValidations.BeginsWithWhitespace)))
        val invalidCharactersErr = invalidCharacters.left.getOrElse(fail("should not accept invalid words"))
        invalidCharactersErr shouldBe NonEmptyList.of(InvalidWord(NonEmptyList.of(GameWordValidations.InvalidCharacter('$'))))
        val multipleWhiteSpaceErr = multipleWhiteSpace.left.getOrElse(fail("should not accept invalid words"))
        multipleWhiteSpaceErr shouldBe NonEmptyList.of(InvalidWord(NonEmptyList.one(HasMultiWhiteSpaceContiguous)))
      }
    }

  }
}
