package pt.porchgeese.hangman

import java.util.UUID

import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import pt.porchgeese.hangman.domain.{GameId, GameWord, GameWordId, Matchup, MatchupId, MatchupState, PlayerId, WhiteSpaceCharacter, WordCharacter}
import pt.porchgeese.hangman.validations.GameWordValidations.{BeginsWithWhitespace, EndsWithWhitespace, HasMultiWhiteSpaceContiguous, InvalidCharacter, IsEmpty}
import pt.porchgeese.hangman.validations.{GameWordValidations, MatchupValidations, Validations}

class ValidationsTests extends AnyFreeSpec with Matchers {
  val (uuid01, uuid02, uuid03, uuid04, uuid05) = (
    UUID.randomUUID(),
    UUID.randomUUID(),
    UUID.randomUUID(),
    UUID.randomUUID(),
    UUID.randomUUID()
  )
  ".exists" - {
    "returns Invalid if None is provided" in {
      Validations.exits(None) shouldBe Invalid(())
    }
    "returns Valid if Some is provided" in {
      Validations.exits(Some("")) shouldBe Valid("")
    }
  }

  ".isNotEmpty" - {
    "returns invalid if empty is provided" in {
      Validations.isNotEmpty(Nil) shouldBe Invalid(())
    }
    "returns valid if a non empty list is provided" in {
      Validations.isNotEmpty(List(1)) shouldBe Valid(NonEmptyList.one(1))
    }
  }

  ".matchupHas2Players" - {
    "returns invalid if the second player is not defined" in {
      MatchupValidations.matchupHas2Players(Matchup(MatchupId(uuid01), PlayerId(uuid02), MatchupState.Paired, None, 0L)) shouldBe Invalid(())
    }
    "return valid if the second player is defined" in {
      MatchupValidations.matchupHas2Players(Matchup(MatchupId(uuid01), PlayerId(uuid02), MatchupState.Paired, Some(PlayerId(uuid03)), 0L)) shouldBe Valid((PlayerId(uuid02), PlayerId(uuid03)))
    }
  }

  ".matchupIsReady" - {
    "returns valid if the state is paired" in {
      MatchupValidations.matchupIsReady(Matchup(MatchupId(uuid01), PlayerId(uuid02), MatchupState.Paired, None, 0L)) shouldBe Valid(())
    }
    "return valid if otherwise" in {
      val matchup = Matchup(MatchupId(uuid01), PlayerId(uuid02), MatchupState.Cancelled, Some(PlayerId(uuid03)), 0L)
      MatchupValidations.matchupIsReady(matchup) shouldBe Invalid(())
      MatchupValidations.matchupIsReady(matchup.copy(state = MatchupState.Waiting)) shouldBe Invalid(())
      MatchupValidations.matchupIsReady(matchup.copy(state = MatchupState.Playing)) shouldBe Invalid(())
    }
  }

  ".playerMatches" - {
    val matchup = Matchup(MatchupId(uuid01), PlayerId(uuid02), MatchupState.Cancelled, Some(PlayerId(uuid03)), 0L)

    "returns valid if the player is part of the matchup" in {
      MatchupValidations.playerMatches(PlayerId(uuid02), matchup) shouldBe Valid(())
      MatchupValidations.playerMatches(PlayerId(uuid03), matchup) shouldBe Valid(())
    }
    "return valid if otherwise" in {
      val matchup = Matchup(MatchupId(uuid01), PlayerId(uuid02), MatchupState.Cancelled, Some(PlayerId(uuid03)), 0L)
      MatchupValidations.playerMatches(PlayerId(uuid05), matchup) shouldBe Invalid(())

    }
  }
//-----
  ".noMoreSubmissions" - {
    "returns valid if the list of submissions is empty" in {
      GameWordValidations.noMoreSubmissions(Nil) shouldBe Valid(())
    }
    "returns invalid if the list of submissions is empty" in {
      GameWordValidations.noMoreSubmissions(List(GameWord(GameWordId(uuid01), GameId(uuid02), PlayerId(uuid03), "", 0L))) shouldBe Invalid(())
    }
  }
  val allowedWordSymbols       = Set(WordCharacter('c'))
  val allowedWhitespaceSymbols = Set(WhiteSpaceCharacter(' '))
  val validWord                = NonEmptyList.of(WordCharacter('c'), WhiteSpaceCharacter(' '), WordCharacter('c'))
  ".endsWithLetter" - {
    "returns valid if the last character of a word is a letter " in {
      GameWordValidations.endsWithLetter(validWord) shouldBe Valid(())
    }
    "returns invalid if the last character of a word is not a letter" in {
      GameWordValidations.endsWithLetter(validWord.append(WhiteSpaceCharacter(' '))) shouldBe Invalid(EndsWithWhitespace)
    }
  }

  ".startsWithLetter" - {
    "returns valid if the first character of a word is a letter " in {
      GameWordValidations.startsWithLetter(validWord) shouldBe Valid(())
    }
    "returns invalid if the first character of a word is not a letter" in {
      GameWordValidations.startsWithLetter(validWord.prepend(WhiteSpaceCharacter(' '))) shouldBe Invalid(BeginsWithWhitespace)
    }
  }

  ".wordIsNotEmpty" - {
    "returns valid if a list of chars is empty" in {
      GameWordValidations.wordIsNotEmpty(List('a')) shouldBe Valid(NonEmptyList.one('a'))
    }
    "returns invalid if a list of chars is empty" in {
      GameWordValidations.wordIsNotEmpty(Nil) shouldBe Invalid(IsEmpty)
    }
  }

  ".hasNoInvalidCharacters" - {
    "returns a invalid word if invalid characters is found" in {
      GameWordValidations.hasNoInvalidCharacters(allowedWordSymbols, allowedWhitespaceSymbols)(
        NonEmptyList.of('c', 'c', 'c', 'c', 'c', ' ', 'a', 'd', '!')
      ) shouldBe Invalid(NonEmptyList.of(InvalidCharacter('a'), InvalidCharacter('d'), InvalidCharacter('!')))
    }

    "returns a valid word if no invalid characters are found" in {
      GameWordValidations.hasNoInvalidCharacters(allowedWordSymbols, allowedWhitespaceSymbols)(
        NonEmptyList.of('c', 'c', ' ', 'c', 'c')
      ) shouldBe Valid(
        NonEmptyList.of(
          WordCharacter('c'),
          WordCharacter('c'),
          WhiteSpaceCharacter(' '),
          WordCharacter('c'),
          WordCharacter('c')
        )
      )
    }
  }

  ".hasNoWhitesapceSequences" - {
    "returns invalid if a word has one ore more contiguous whitespace" in {
      val whiteSpaceSequence = NonEmptyList.of(WhiteSpaceCharacter(' '), WhiteSpaceCharacter(' '))
      GameWordValidations.hasNoWhitespaceSequences(validWord.concatNel(whiteSpaceSequence).concatNel(validWord)) shouldBe Invalid(HasMultiWhiteSpaceContiguous)

    }
    "returns valid if a word has no contiguous whitespace" in {
      GameWordValidations.hasNoWhitespaceSequences(validWord) shouldBe Valid(())
    }
  }

}
