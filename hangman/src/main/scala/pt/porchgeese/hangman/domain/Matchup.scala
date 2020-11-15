package pt.porchgeese.hangman.domain

import java.util.UUID

import doobie.{Put, Read, Write}
import MatchupState.MatchupState
import cats.Show
import doobie.postgres.implicits._
import cats.implicits._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

case class Matchup(id: MatchupId, player: PlayerId, state: MatchupState, player2: Option[PlayerId], createdAt: Long)

object MatchupState {
  sealed trait MatchupState
  final case object Waiting   extends MatchupState
  final case object Cancelled extends MatchupState
  final case object Paired    extends MatchupState
  final case object Playing   extends MatchupState

  def fromString(s: String): Either[String, MatchupState] =
    s match {
      case "WAITING"   => Waiting.asRight
      case "CANCELLED" => Cancelled.asRight
      case "PAIRED"    => Paired.asRight
      case "PLAYING"   => Playing.asRight
      case other       => s"Invalid value for matchup state provided: ${other}".asLeft
    }

  implicit val show: Show[MatchupState] = implicitly[Show[String]].contramap {
    case Waiting   => "WAITING"
    case Cancelled => "CANCELLED"
    case Paired    => "PAIRED"
    case Playing   => "PLAYING"
  }

  implicit val writer: Put[MatchupState] = Put[String].tcontramap(show.show)

  implicit val reader: Read[MatchupState] = Read[String].map { x =>
    fromString(x) match {
      case Left(value)  => throw new RuntimeException(value)
      case Right(value) => value
    }
  }

  implicit val dec: Decoder[MatchupState] = implicitly[Decoder[String]].emap(fromString)
  implicit val enc: Encoder[MatchupState] = implicitly[Encoder[String]].contramap(show.show)
}

object Matchup {
  def newMatchup(id: MatchupId, playerId: PlayerId, currentTime: Long): Matchup = Matchup(id, playerId, MatchupState.Waiting, None, currentTime)
  implicit val writer: doobie.Write[Matchup]                                    = Write[(MatchupId, PlayerId, MatchupState, Option[PlayerId], Long)].contramap(Matchup.unapply(_).get)
  implicit val reader: doobie.Read[Matchup]                                     = Read[(MatchupId, PlayerId, MatchupState, Option[PlayerId], Long)].map((Matchup.apply _).tupled)
  implicit val dec: Decoder[Matchup]                                            = deriveDecoder[Matchup]
  implicit val enc: Encoder[Matchup]                                            = deriveEncoder[Matchup]
}

case class MatchupId(v: UUID) extends AnyVal
object MatchupId {
  implicit val write: Put[MatchupId]   = Put[UUID].tcontramap(_.v)
  implicit val reade: Read[MatchupId]  = Read[UUID].map(MatchupId(_))
  implicit val dec: Decoder[MatchupId] = implicitly[Decoder[UUID]].map(MatchupId(_))
  implicit val enc: Encoder[MatchupId] = implicitly[Encoder[UUID]].contramap(_.v)
}
