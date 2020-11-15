package pt.porchgeese.hangman.domain
import java.util.UUID
import doobie.postgres.implicits._

import doobie.{Put, Read, Write}

final case class Guess(id: GuessId, letter: Char, gameId: GameId, player: PlayerId, createdAt: Long)
object Guess {

  implicit val writer: Write[Guess] = Write[(GuessId, String, GameId, PlayerId, Long)].contramap(g => Guess.unapply(g).get.copy(_2 = g.letter.toString))
  implicit val reader: Read[Guess]  = Read[(GuessId, String, GameId, PlayerId, Long)].map(t => Guess(t._1, t._2.head, t._3, t._4, t._5))
}
final case class GuessId(id: UUID)

object GuessId {
  implicit val write: Put[GuessId]  = Put[UUID].tcontramap(_.id)
  implicit val reade: Read[GuessId] = Read[UUID].map(GuessId(_))
}
