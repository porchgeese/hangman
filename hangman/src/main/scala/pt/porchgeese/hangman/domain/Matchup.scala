package pt.porchgeese.hangman.domain

import java.util.UUID

import doobie.{Put, Read, Write}
import MatchupState.MatchupState
import doobie.postgres._
import doobie.postgres.implicits._
import fs2.kafka.Timestamp

case class Matchup(id: MatchupId, player: PlayerId, state: MatchupState, player2: Option[PlayerId], createdAt: Long)

object MatchupState {
  sealed trait MatchupState
  case object Waiting   extends MatchupState
  case object Cancelled extends MatchupState
  case object Paired    extends MatchupState
  case object Playing   extends MatchupState

  implicit val writer: Put[MatchupState] = Put[String].tcontramap {
    case Waiting   => "WAITING"
    case Cancelled => "CANCELLED"
    case Paired    => "PAIRED"
    case Playing   => "PLAYING"
  }
}

object Matchup {
  def newMatchup(id: MatchupId, playerId: PlayerId, currentTime: Long): Matchup = Matchup(id, playerId, MatchupState.Waiting, None, currentTime)
  implicit val writer: doobie.Write[Matchup]                                    = Write[(MatchupId, PlayerId, MatchupState, Option[PlayerId], Long)].contramap(Matchup.unapply(_).get)
}

case class MatchupId(v: UUID) extends AnyVal
object MatchupId {
  implicit val write: Put[MatchupId]  = Put[UUID].tcontramap(_.v)
  implicit val reade: Read[MatchupId] = Read[UUID].map(MatchupId(_))
}
