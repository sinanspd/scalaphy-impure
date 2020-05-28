package app.vizion.exampleProject.routes

import cats.effect.Sync
import io.circe.refined._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final class ReviewRoutes[F[_]: Sync](
    movies: Reviews[F]
)extends Http4sDsl[F]{

    private[routes] val uri = "/reviews"

    private val authenticatedRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of{
        case GET -> Root => Ok(review.getReviews)
        case GET -> Root / id => Ok(reviews.getReviewById(id))
        case ar @ POST -> Root as _ => 
            ar.req.decodeR[NewReviewRequest]{bp =>
                Created(reviews.createReview(bp))
            }
        case put @ PUT -> Root / id as _ =>
            ar.req.decodeR[NewReviewRequest]{bp =>
                Updated(reviews.updateReview(id, bp))   
            }
        case DELETE -> Root / id as _ => Deleted(reviews.deleteReview(id))
    }

    def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
        prefixPath -> authMiddleware(authenticatedRoutes)
    )
}