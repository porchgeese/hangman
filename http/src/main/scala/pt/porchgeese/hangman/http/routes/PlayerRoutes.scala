package pt.porchgeese.hangman.http.routes

import cats.effect.IO
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._
import pt.porchgeese.hangman.http.routes.PlayerRoutes.NewPlayer
import pt.porchgeese.hangman.services.PlayerService

class PlayerRoutes(playerService: PlayerService) {
  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "player" =>
        for {
          player    <- req.as[NewPlayer]
          newPlayer <- playerService.createPlayer(player.name)
          response  <- Created(newPlayer)
        } yield response
    }
}

object PlayerRoutes {
  case class NewPlayer(name: String)
  object NewPlayer {
    implicit val dec: Decoder[NewPlayer] = deriveDecoder[NewPlayer]
    implicit val enc: Encoder[NewPlayer] = deriveEncoder[NewPlayer]
  }
}
