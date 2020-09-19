package pt.porchgeese.hangman.services

import java.util.UUID

import cats.effect.IO

class IdGeneratorService {
  def generateId: IO[UUID] = IO(UUID.randomUUID())
}
