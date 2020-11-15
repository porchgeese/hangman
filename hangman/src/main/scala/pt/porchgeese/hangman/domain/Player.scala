package pt.porchgeese.hangman.domain

import java.util.UUID

import doobie.{Put, Read}
import doobie.postgres.implicits._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class Player(id: PlayerId, name: String, createdAt: Long)
object Player {
  def newPlayer(id: PlayerId, name: String, createdAt: Long): Player = Player(id, name, createdAt)
  implicit val encoder: Encoder[Player]                              = deriveEncoder[Player]
  implicit val decoder: Decoder[Player]                              = deriveDecoder[Player]
}

final case class PlayerId(value: UUID) extends AnyVal

object PlayerId {
  implicit val writer: Put[PlayerId]  = Put[UUID].tcontramap(_.value)
  implicit val reader: Read[PlayerId] = Read[UUID].map(PlayerId(_))

  implicit val encoder: Encoder[PlayerId] = implicitly[Encoder[UUID]].contramap(_.value)
  implicit val decoder: Decoder[PlayerId] = implicitly[Decoder[UUID]].map(PlayerId(_))
}
