package microservice.common.routes

import cats.effect.Sync
import cats.{Defer, Monad}
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

class HealthCheck[F[_]: Monad: Defer: Sync](hc: F[Map[String, Map[String, Health]]]) extends Http4sDsl[F] {

  implicit val ec: EntityEncoder[F, Map[String, Map[String, Health]]] = jsonEncoderOf[F, Map[String, Map[String, Health]]]

  def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "_meta" / "health" =>
      Ok(hc)
    case GET -> Root / "_meta" / "ping" =>
      Ok(Pong())
  }
}

case class Pong(result: String = "Pong")
object Pong {
  implicit val encoder: Encoder.AsObject[Pong]                   = deriveEncoder[Pong]
  implicit val decoder: Decoder[Pong]                            = deriveDecoder[Pong]
  implicit def entityEncoder[F[_]]: EntityEncoder[F, Pong]       = jsonEncoderOf[F, Pong]
  implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, Pong] = jsonOf[F, Pong]
}

sealed trait Health
case object Healthy   extends Health
case object Unhealthy extends Health
object Health {
  implicit val encoder: Encoder[Health] = deriveEncoder[Health]
  implicit val decoder: Decoder[Health] = deriveDecoder[Health]
}
