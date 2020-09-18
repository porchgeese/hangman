package pt.porchgeese.hangman.database

import doobie.implicits._
import doobie.util.update.Update
import doobie.{ConnectionIO, Fragments}
import pt.porchgeese.hangman.database.GameWordRepository.GameWordFilter
import pt.porchgeese.hangman.domain.{GameId, GameWord, GameWordId, PlayerId}

class GameWordRepository {
  def createGameWord(gw: GameWord): ConnectionIO[Unit] = {
    val query =
      s"""INSERT INTO gameWord(id, gameId, playerId, word, createdAt)
         |VALUES (?,?,?,?,?)
         |""".stripMargin
    Update[GameWord](query).toUpdate0(gw).run.flatMap(database.validateSingleInsert)
  }

  def gameWordsWithFilter(f: GameWordFilter): ConnectionIO[List[GameWord]] =
    (fr"""
         |SELECT id, gameId, playerId, word, createdAt
         |FROM
         |gameWord
         |""".stripMargin ++ Fragments.whereAndOpt(
      f.id.map(id => fr"id = ${id}"),
      f.game.map(gameId => fr"gameId = ${gameId}"),
      f.player.map(playerId => fr"playerId = ${playerId}"),
      f.word.map(word => fr"word = ${word}")
    )).query[GameWord].to[List]
}

object GameWordRepository {
  case class GameWordFilter(
      id: Option[GameWordId] = None,
      game: Option[GameId] = None,
      player: Option[PlayerId] = None,
      word: Option[String] = None
  )
}
