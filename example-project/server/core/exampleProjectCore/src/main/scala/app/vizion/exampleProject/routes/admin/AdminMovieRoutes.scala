//package app.vizion.exampleProject.routes.admin
//
//import cats.effect.Sync
//import io.circe.refined._
//import org.http4s._
//import org.http4s.dsl.Http4sDsl
//import org.http4s.server._
//
//final class AdminMovieRoutes[F[_]: Sync](
//    brands: Movies[F]
//) extends Http4sDsl[F] {
//
//  private[admin] val prefixPath = "/movies"
//
//  private val httpRoutes: AuthedRoutes[AdminUser, F] = AuthedRoutes.of {
//        case put @ PUT -> Root / id as _ =>
//            ar.req.decodeR[NewMovieRequest]{bp =>
//                Updated(movies.updateMovie(id, bp))
//            }
//        case DELETE -> Root / id as _ => Deleted(movies.deleteMovie(id))
//    }
//
//  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
//    prefixPath -> authMiddleware(httpRoutes)
//  )
//}