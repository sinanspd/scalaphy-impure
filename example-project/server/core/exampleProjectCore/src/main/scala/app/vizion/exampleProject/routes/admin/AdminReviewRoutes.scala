//package app.vizion.exampleProject.routes.admin
//
//import app.vizion.exampleProject.algebras.Review
//import app.vizion.exampleProject.auth.schema.auth.AdminUser
//import cats.effect.Sync
//import io.circe.refined._
//import org.http4s._
//import org.http4s.dsl.Http4sDsl
//import org.http4s.server._
//import app.vizion.exampleProject.decoder._
//
//final class AdminReviewRoutes[F[_]: Sync](
//    review: Review[F]
//) extends Http4sDsl[F] {
//
//  private[admin] val prefixPath = "/reviews"
//
//  private val httpRoutes: AuthedRoutes[AdminUser, F] = AuthedRoutes.of {
////        case ar @ PUT -> Root / id as _ =>
////            ar.req.decodeR[NewReviewRequest]{bp =>
////                Ok(review.updateReview(id, bp))
////            }
////        case DELETE -> Root / id as _ => Pl(review.deleteReview(id))
//    }
//
//  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
//    prefixPath -> authMiddleware(httpRoutes)
//  )
//}