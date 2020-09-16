package pt.porchgeese.hangman.database

import doobie.{ConnectionIO, FC}

object database {
  def validateSingleInsert(i: Int): ConnectionIO[Unit] =
    i match {
      case 1     => FC.unit
      case other => FC.raiseError(new RuntimeException(s"Failed to insert a single value in the database. Expected number of inserted values to be 1 but got $other"))
    }
  def validateSingleUpdate(i: Int): ConnectionIO[Unit] =
    i match {
      case 1     => FC.unit
      case other => FC.raiseError(new RuntimeException(s"Failed to update a single value in the database. Expected number of inserted values to be 1 but got $other"))
    }
}
