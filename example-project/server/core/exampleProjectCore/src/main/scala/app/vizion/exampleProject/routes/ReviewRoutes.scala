//package app.vizion.exampleProject.routes
//
//import app.vizion.exampleProject.algebras.Review
//import app.vizion.exampleProject.auth.schema.auth.CommonUser
//import cats.effect.Sync
//import io.circe.refined._
//import org.http4s._
//import org.http4s.dsl.Http4sDsl
//import org.http4s.server._
//import app.vizion.exampleProject.decoder._
//
//final class ReviewRoutes[F[_]: Sync](
//    review: Review[F]
//)extends Http4sDsl[F]{
//
//    private[routes] val uri = "/reviews"
//
//    private val authenticatedRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of{
//        case GET -> Root => Ok(review.getReviews)
//        case GET -> Root / id => Ok(review.getReviewById(id))
//        case ar @ POST -> Root as _ =>
//            ar.req.decodeR[NewReviewRequest]{bp =>
//                Created(review.createReview(bp))
//            }
////        case ar @ PUT -> Root / id as _ =>
////            ar.req.decodeR[NewReviewRequest]{bp =>
////                Ok(review.updateReview(id, bp))
////            }
//        case DELETE -> Root / id as _ => Ok(review.deleteReviewById(id))
//    }
//
//    def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
//        uri -> authMiddleware(authenticatedRoutes)
//    )
//}