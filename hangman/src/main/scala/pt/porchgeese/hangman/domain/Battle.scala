package pt.porchgeese.hangman.domain

import java.util.UUID

case class Battle(id: UUID, guessingPlayer: Player, wordPlayer: Player, guesses: List[Guess], word: Word)
