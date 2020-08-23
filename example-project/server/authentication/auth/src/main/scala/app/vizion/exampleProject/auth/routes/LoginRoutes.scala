package app.vizion.exampleProject.auth.http.routes

import cats.effect.Sync
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import app.vizion.exampleProject.auth.algebras.Auth
import app.vizion.exampleProject.auth.http.requests.LoginUser
import app.vizion.exampleProject.auth.schema.auth._
import app.vizion.exampleProject.auth.http.decoder._
import app.vizion.exampleProject.auth.utils.json._

final class LoginRoutes[F[_]: Sync](
    auth: Auth[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "login" =>
      req.decodeR[LoginUser] { user =>
        auth
          .login(user.username.toDomain, user.password.toDomain)
          .flatMap(Ok(_))
          .handleErrorWith {
            case InvalidUserOrPassword(_) => Forbidden()
          }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
