package app.vizion.exampleProject.routes

import cats.effect.Sync
import io.circe.refined._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final class MovieRoutes[F[_]: Sync](
    movies: Movies[F]
)extends Http4sDsl[F]{

    private[routes] val uri = "/movies"

    private val authenticatedRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of{
        case GET -> Root => Ok(movies.getMovies)
        case GET -> Root / id => Ok(movies.getMovieById(id))
        case ar @ POST -> Root as _ => 
            ar.req.decodeR[NewMovieRequest]{bp =>
                Created(movies.createMovie(bp))
            }
        case put @ PUT -> Root / id as _ =>
            ar.req.decodeR[NewMovieRequest]{bp =>
                Updated(movies.updateMovie(id, bp))   
            }
        case DELETE -> Root / id as _ => Deleted(movies.deleteMovie(id))
    }

    def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
        prefixPath -> authMiddleware(authenticatedRoutes)
    )
}