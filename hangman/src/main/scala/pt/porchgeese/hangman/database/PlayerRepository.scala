package pt.porchgeese.hangman.database

import doobie.ConnectionIO
import doobie.util.update.Update
import pt.porchgeese.hangman.domain.Player

class PlayerRepository {

  def createtUser(player: Player): ConnectionIO[Unit] = {
    val query =
      s"""INSERT INTO player(id, name, createdAt)
         |VALUES (?,?,?)
         |""".stripMargin
    Update[Player](query).toUpdate0(player).run.flatMap(database.validateSingleInsert)
  }

}
