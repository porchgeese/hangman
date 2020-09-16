package pt.porchgeese.hangman.domain

import java.util.UUID

case class Guess(id: UUID, char: Char, creationTime: Long)
