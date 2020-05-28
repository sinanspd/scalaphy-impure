package app.vizion.exampleProject.routes.admin

import cats.effect.Sync
import io.circe.refined._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final class AdminReviewRoutes[F[_]: Sync](
    brands: Reviews[F]
) extends Http4sDsl[F] {

  private[admin] val prefixPath = "/reviews"

  private val httpRoutes: AuthedRoutes[AdminUser, F] = AuthedRoutes.of {
        case put @ PUT -> Root / id as _ =>
            ar.req.decodeR[NewReviewRequest]{bp =>
                Updated(reviews.updateReview(id, bp))   
            }
        case DELETE -> Root / id as _ => Deleted(reviews.deleteReview(id))
    }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}