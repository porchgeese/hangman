package pt.porchgeese.hangman.database

import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.update.Update
import pt.porchgeese.hangman.domain.{Game, GameId}
class GameRepository {
  def creatGame(m: Game): ConnectionIO[Unit] = {
    val query =
      s"""INSERT INTO game(id, matchupId, state, createdAt)
         |VALUES (?,?,?,?)
         |""".stripMargin
    Update[Game](query).toUpdate0(m).run.flatMap(database.validateSingleInsert)
  }

  def findGame(gameId: GameId): doobie.ConnectionIO[Option[Game]] =
    sql"""
       |SELECT id, matchupId, state, createdAt
       |FROM game
       |WHERE id=${gameId}
       |""".stripMargin.query[Game].option

}
