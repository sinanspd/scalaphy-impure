package app.vizion.exampleProject.routes.admin

import java.util.UUID

import app.vizion.exampleProject.algebras.Movies
import app.vizion.exampleProject.auth.schema.auth.AdminUser
import app.vizion.exampleProject.requests.NewMovieRequest
import cats.effect.Sync
import io.circe.refined._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import app.vizion.exampleProject.json._

final class AdminMovieRoutes[F[_]: Sync](
    movies: Movies[F]
) extends Http4sDsl[F] {

  private[admin] val prefixPath = "/movies"

  private val httpRoutes: AuthedRoutes[AdminUser, F] = AuthedRoutes.of {
//        case put @ PUT -> Root / id as _ =>
//            ar.req.decodeR[NewMovieRequest]{bp =>
//                Ok(movies.updateMovie(id, bp))
//            }
    case DELETE -> Root / id as _ => Ok(movies.deleteMovieById(UUID.fromString(id)))
  }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
