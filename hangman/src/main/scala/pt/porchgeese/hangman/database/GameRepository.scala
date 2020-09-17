package pt.porchgeese.hangman.database

import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.update.Update
import pt.porchgeese.hangman.domain.MatchupState.{MatchupState, Paired, Waiting}
import pt.porchgeese.hangman.domain.{Matchup, MatchupId, PlayerId}

class GameRepository {
  def createMatchup(m: Matchup): ConnectionIO[Unit] = {
    val query =
      s"""INSERT INTO matchup(id, player, state, player2, createdAt)
         |VALUES (?,?,?,?,?)
         |""".stripMargin
    Update[Matchup](query).toUpdate0(m).run.flatMap(database.validateSingleInsert)
  }

  def findAvailableMatchupWithLock(): ConnectionIO[Option[MatchupId]] = {
    val waiting: MatchupState = Waiting
    sql"""
         |SELECT id
         |FROM matchup
         |WHERE
         |  player2 IS NULL AND
         |  state=${waiting}
         |ORDER BY createdAt asc
         |FOR UPDATE SKIP LOCKED
         |LIMIT 1
         |;
         |""".stripMargin
      .query[MatchupId]
      .option
  }

  def pairPlayerWithMatchup(p: PlayerId, m: MatchupId): ConnectionIO[Unit] = {
    val paired: MatchupState = Paired
    val query =
      sql"""
         |UPDATE matchup
         |SET
         |  state=${paired},
         |  player2=${p}
         |WHERE
         |  id=$m;
         |""".stripMargin
    query.update.run.flatMap(database.validateSingleUpdate)
  }

}
