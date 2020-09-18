package pt.porchgeese.hangman.domain

sealed trait GameCharacter
final case class WhiteSpaceCharacter(c: Char) extends GameCharacter
final case class WordCharacter(c: Char)       extends GameCharacter
