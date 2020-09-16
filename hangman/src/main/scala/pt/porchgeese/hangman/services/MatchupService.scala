package pt.porchgeese.hangman.services

import cats.effect.{Clock, IO}
import doobie.{ConnectionIO, FC}
import doobie.util.transactor.Transactor
import pt.porchgeese.hangman.domain.PlayerId
import doobie.implicits._
import cats.implicits._

import scala.concurrent.duration._
import MatchupService.{MatchupCreated, MatchupFound}
import pt.porchgeese.hangman.database.MatchupRepository
import pt.porchgeese.hangman.domain.{Matchup, MatchupId, PlayerId}
class MatchupService(
    matchupRepo: MatchupRepository,
    db: Transactor[IO],
    idGenerator: IdGeneratorService,
    clock: Clock[IO]
) {
  def createMatchup(p: PlayerId): IO[Matchup] =
    for {
      matchupId <- idGenerator.generateId.map(MatchupId(_))
      now       <- clock.realTime(MILLISECONDS)
      matchup = Matchup.newMatchup(matchupId, p, now)
      _ <- matchupRepo.createMatchup(matchup).transact(db)
    } yield matchup

  def matchupPlayer(p: PlayerId): IO[Either[MatchupCreated, MatchupFound]] =
    for {
      matchupResult <- (for {
          matchupId <- matchupRepo.findAvailableMatchupWithLock()
          _         <- matchupId.fold[ConnectionIO[Unit]](FC.unit)(id => matchupRepo.pairPlayerWithMatchup(p, id))
        } yield matchupId).transact(db)
      result <- matchupResult match {
        case Some(id) => IO.pure(MatchupFound(id).asRight)
        case None     => createMatchup(p).map(MatchupCreated(_).asLeft)
      }
    } yield result
}

object MatchupService {
  case class MatchupCreated(matchup: Matchup)
  case class MatchupFound(matchup: MatchupId)
}
