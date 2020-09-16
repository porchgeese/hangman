package pt.porchgeese.hangman.domain

import java.util.UUID

case class War(id: UUID, player1: Player, player2: Player, battles: Battle)
