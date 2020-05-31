package app.vizion.exampleProject.routes

import app.vizion.exampleProject.algebras.Movies
import app.vizion.exampleProject.auth.schema.auth.CommonUser
import cats.effect.Sync
import io.circe.refined._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import cats.implicits._
import app.vizion.exampleProject.decoder._
import app.vizion.exampleProject.requests.NewMovieRequest
import java.util.UUID
import app.vizion.exampleProject.json._
import app.vizion.exampleProject.schema.movies._

import app.vizion.exampleProject.schema.movies.{MovieDescription, MovieName, MovieYear}

final class MovieRoutes[F[_]: Sync](
    movies: Movies[F]
)extends Http4sDsl[F]{

    private[routes] val uri = "/movies"

    private val authenticatedRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of{
        case GET -> Root as _  => Ok(movies.getMovies)
        case GET -> Root / id  as _ => Ok(movies.getMovieById(UUID.fromString(id)))
        case ar @ POST -> Root as _ =>
            ar.req.decodeR[NewMovieRequest]{bp =>
                Created(movies.createMovie(MovieName(bp.name), MovieYear(bp.year.toString), MovieDescription(bp.description), stringToGenre(bp.genre)))
            }
//        case put @ PUT -> Root / id as _ =>
//            ar.req.decodeR[NewMovieRequest]{bp =>
//                Updated(movies.updateMovie(id, bp))
//            }
        case DELETE -> Root / id as _ => Ok(movies.deleteMovieById(UUID.fromString(id)))
    }

    def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
        uri -> authMiddleware(authenticatedRoutes)
    )
}