package pt.porchgeese.hangman.domain

import java.util.UUID

import doobie.{Put, Read}
import doobie.postgres.implicits._

case class Player(id: PlayerId, name: String, createdAt: Long)
object Player {
  def newPlayer(id: PlayerId, name: String, createdAt: Long): Player = Player(id, name, createdAt)
}

final case class PlayerId(value: UUID) extends AnyVal

object PlayerId {
  implicit val writer: Put[PlayerId]  = Put[UUID].tcontramap(_.value)
  implicit val reader: Read[PlayerId] = Read[UUID].map(PlayerId(_))
}
