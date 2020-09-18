package pt.porchgeese.hangman.services

import cats.effect.{Clock, IO}
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import doobie.{ConnectionIO, FC}
import pt.porchgeese.hangman.database.{MatchupRepository, PlayerRepository}
import pt.porchgeese.hangman.domain.{Matchup, MatchupId, Player, PlayerId}
import pt.porchgeese.hangman.services.MatchupService.{MatchupCreated, MatchupFound}

import scala.concurrent.duration._

class PlayerService(
    playerRepo: PlayerRepository,
    db: Transactor[IO],
    idGenerator: IdGeneratorService,
    clock: Clock[IO]
) {
  def createPlayer(name: String): IO[Player] =
    for {
      playerId <- idGenerator.generateId.map(PlayerId(_))
      now      <- clock.realTime(MILLISECONDS)
      user = Player.newPlayer(playerId, name, now)
      _ <- playerRepo.createtUser(user).transact(db)
    } yield user
}
