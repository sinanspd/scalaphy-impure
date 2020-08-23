package app.vizion.exampleProject.auth.http

import cats.Monad
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import io.circe._
import io.circe.generic.semiauto._
import io.circe.refined._
import app.vizion.exampleProject.auth.http.requests._
import app.vizion.exampleProject.auth.utils.json._

object decoder {

  implicit class RefinedRequestDecoder[F[_]: Monad](req: Request[F]) extends Http4sDsl[F] {

    def decodeR[A](f: A => F[Response[F]])(implicit ev: EntityDecoder[F, A]): F[Response[F]] =
      ev.decode(req, strict = false).value.flatMap {
        case Left(e) =>
          e.cause match {
            case Some(c) if c.getMessage.startsWith("Predicate") => BadRequest(c.getMessage)
            case _                                               => UnprocessableEntity()
          }
        case Right(a) => f(a)
      }
  }

  implicit val createUserDecoder: Decoder[CreateUser] = deriveDecoder[CreateUser]
  implicit val loginUserDecoder: Decoder[LoginUser]   = deriveDecoder[LoginUser]
}
