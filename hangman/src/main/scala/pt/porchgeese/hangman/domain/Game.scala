package pt.porchgeese.hangman.domain

import java.util.UUID

import doobie.{Put, Read}
import doobie.postgres.implicits._
import pt.porchgeese.hangman.domain.GameState.{GameState, Ready}

case class Game(id: GameId, matchupId: MatchupId, state: GameState, createdAt: Long)

object Game {
  def newMatchup(id: GameId, matchup: MatchupId, createdAt: Long): Game = Game(id, matchup, Ready, createdAt)
}

case class GameId(value: UUID) extends AnyVal

object GameId {
  implicit val writer: Put[GameId]  = Put[UUID].tcontramap(_.value)
  implicit val reader: Read[GameId] = Read[UUID].map(GameId(_))
}

object GameState {
  sealed trait GameState
  final case object Ready    extends GameState
  final case object Playing  extends GameState
  final case object Finished extends GameState

  implicit val writer: Put[GameState] = Put[String].tcontramap {
    case Ready    => "READY"
    case Playing  => "PLAYING"
    case Finished => "FINISHED"
  }

  implicit val reader: Read[GameState] = Read[String].map(_.toUpperCase).map {
    case "READY"    => Ready
    case "PLAYING"  => Playing
    case "FINISHED" => Finished
  }

}
