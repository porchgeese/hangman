package pt.porchgeese.hangman.domain

import java.util.UUID

import doobie.{Put, Write}
import doobie.postgres.implicits._
import doobie.postgres._

case class Player(id: PlayerId, name: String)
case class PlayerId(value: UUID) extends AnyVal

object PlayerId {
  implicit val writer: Put[PlayerId] = Put[UUID].tcontramap(_.value)
}
