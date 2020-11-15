package pt.porchgeese.hangman.database

import doobie.{ConnectionIO, Fragments}
import doobie.implicits._
import doobie.util.update.Update
import pt.porchgeese.hangman.database.GuessRepository.GuessFilter
import pt.porchgeese.hangman.domain.{GameId, Guess, GuessId, PlayerId}

class GuessRepository {
  def createGuess(g: Guess): ConnectionIO[Unit] = {
    val query =
      s"""INSERT INTO guess(id, letter, gameId, playerId, createdAt)
         |VALUES (?,?,?,?,?)
         |""".stripMargin
    Update[Guess](query).toUpdate0(g).run.flatMap(database.validateSingleInsert)
  }

  def findGuesses(f: GuessFilter): ConnectionIO[List[Guess]] =
    (fr"""
           |SELECT id, letter, gameId, playerId, createdAt
           |FROM
           |guess
           |""".stripMargin ++ Fragments.whereAndOpt(
      f.id.map(id => fr"id = ${id}"),
      f.letter.map(letter => fr"guess = ${letter.toString}"),
      f.gameId.map(gameId => fr"gameId = ${gameId}"),
      f.playerId.map(playerId => fr"playerId = ${playerId}")
    )).query[Guess].to[List]
}

object GuessRepository {
  case class GuessFilter(
      id: Option[GuessId] = None,
      letter: Option[Char] = None,
      gameId: Option[GameId] = None,
      playerId: Option[PlayerId] = None
  )
}
