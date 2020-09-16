package pt.porchgeese.hangman.services

import java.util.UUID

import cats.effect.IO
import doobie.util.transactor.Transactor
import pt.porchgeese.hangman.domain.MatchupId
import pt.porchgeese.hangman.database.MatchupRepository

class IdGeneratorService {
  def generateId: IO[UUID] = IO(UUID.randomUUID())
}
