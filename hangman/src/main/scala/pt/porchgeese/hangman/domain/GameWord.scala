package pt.porchgeese.hangman.domain

import java.util.UUID

import doobie.{Put, Read, Write}
import doobie.postgres.implicits._

final case class GameWord(id: GameWordId, gameId: GameId, playerId: PlayerId, word: String, createdAt: Long)
object GameWord {

  def newGameWord(id: GameWordId, gameId: GameId, playerId: PlayerId, word: String, createdAt: Long): GameWord =
    GameWord(id, gameId, playerId, word, createdAt)

  implicit val writer: Write[GameWord] = Write[(GameWordId, GameId, PlayerId, String, Long)].contramap(GameWord.unapply(_).get)
  implicit val reader: Read[GameWord]  = Read[(GameWordId, GameId, PlayerId, String, Long)].map((GameWord.apply _).tupled)
}

final case class GameWordId(id: UUID) extends AnyVal
object GameWordId {
  implicit val writer: Put[GameWordId]  = Put[UUID].tcontramap(_.id)
  implicit val reader: Read[GameWordId] = Read[UUID].map(GameWordId(_))
}
