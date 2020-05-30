package app.vizion.exampleProject.routes

import java.util.UUID

import app.vizion.exampleProject.algebras.Review
import app.vizion.exampleProject.auth.schema.auth.CommonUser
import cats.effect.Sync
import io.circe.refined._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import app.vizion.exampleProject.decoder._
import app.vizion.exampleProject.requests.NewReviewRequest
import app.vizion.exampleProject.schema.reviews.{ReviewBody, ReviewDate, UserId}
import app.vizion.exampleProject.json._

final class ReviewRoutes[F[_]: Sync](
    review: Review[F]
)extends Http4sDsl[F]{

    private[routes] val uri = "/reviews"

    private val authenticatedRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of{
        case GET -> Root as _ => Ok(review.getReviews)
        case GET -> Root / id as _ => Ok(review.getReviewById(UUID.fromString(id)))
        case ar @ POST -> Root as _ =>
            ar.req.decodeR[NewReviewRequest]{ bp =>
                Created(review.createReview(UserId(bp.user), ReviewBody(bp.body), ReviewDate(bp.date)))
            }
//        case ar @ PUT -> Root / id as _ =>
//            ar.req.decodeR[NewReviewRequest]{bp =>
//                Ok(review.updateReview(id, bp))
//            }
        case DELETE -> Root / id as _ => Ok(review.deleteReviewById(UUID.fromString(id)))
    }

    def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
        uri -> authMiddleware(authenticatedRoutes)
    )
}