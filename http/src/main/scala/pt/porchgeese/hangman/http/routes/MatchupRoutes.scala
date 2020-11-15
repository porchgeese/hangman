package pt.porchgeese.hangman.http.routes

import cats.effect.IO
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.io._
import pt.porchgeese.hangman.domain.PlayerId
import pt.porchgeese.hangman.http.routes.MatchupRoutes.JoinMatchupRequest
import pt.porchgeese.hangman.services.MatchupService

class MatchupRoutes(matchup: MatchupService) {
  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "matchup" / "join" =>
        for {
          player  <- req.as[JoinMatchupRequest]
          matchup <- matchup.matchupPlayer(player.player)
          response <- matchup match {
            case Right(value) => Ok(value.matchup)
            case Left(value)  => Created(value.matchup)
          }
        } yield response
      case req @ POST -> Root / "matchup" =>
        for {
          player   <- req.as[JoinMatchupRequest]
          matchup  <- matchup.createMatchup(player.player)
          response <- Ok(matchup)
        } yield response
    }
}

object MatchupRoutes {
  case class JoinMatchupRequest(player: PlayerId)
  object JoinMatchupRequest {
    implicit val dec: Decoder[JoinMatchupRequest] = deriveDecoder[JoinMatchupRequest]
    implicit val enc: Encoder[JoinMatchupRequest] = deriveEncoder[JoinMatchupRequest]
  }
}
