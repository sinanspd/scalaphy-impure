package app.vizion.exampleProject.modules

import app.vizion.exampleProject.auth.modules.AuthModule
import app.vizion.exampleProject.errors.{HttpErrorHandler, RoutesHttpErrorHandler}
import app.vizion.exampleProject.routes.{MovieRoutes, ReviewRoutes, version}
import app.vizion.exampleProject.routes.admin.{AdminMovieRoutes, AdminReviewRoutes}
import cats.effect._
import cats.implicits._
import com.olegpy.meow.hierarchy._
import dev.profunktor.auth.JwtAuthMiddleware
import dev.profunktor.auth.jwt.JwtToken
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.middleware._
import org.http4s.server.Router
import pdi.jwt._

import scala.concurrent.duration._
import cats.{ApplicativeError, MonadError}
import cats.data.{Kleisli, OptionT}
import app.vizion.exampleProject.auth.algebras
import app.vizion.exampleProject.auth.schema.auth.{AdminUser, CommonUser}

import scala.util.control.NoStackTrace

object Errors {
  case class SomeError(value: String) extends NoStackTrace // Delete this
}


object HttpApi{
    def make[F[_]: Concurrent : Timer](
        algebras: Algebras[F],
        security: AuthModule[F]
    ): F[HttpApi[F]] =
        Sync[F].delay(
            new HttpApi[F](
                algebras,
                security
            )
        )
}


final class HttpApi[F[_]: Concurrent : Timer] private (
    algebras: Algebras[F],
    security: AuthModule[F]
){

  import Errors._

  private val adminAuth: JwtToken => JwtClaim => F[Option[AdminUser]] =
    t => c => security.adminAuth.findUser(t)(c)
  private val usersAuth: JwtToken => JwtClaim => F[Option[CommonUser]] =
    t => c => security.usersAuth.findUser(t)(c)

  private val adminMiddleware = JwtAuthMiddleware[F, AdminUser](security.adminJwtAuth.value, adminAuth)
  private val userMiddleware = JwtAuthMiddleware[F, CommonUser](security.userJwtAuth.value, usersAuth)


  private implicit val errorHandler: HttpErrorHandler[F, SomeError] = ErrorHandler[F]

    val movieRoutes = new MovieRoutes[F](algebras.movies).routes(userMiddleware)
    val reviewRoutes = new ReviewRoutes[F](algebras.reviews).routes(userMiddleware)

    val adminMovieRoutes = new AdminMovieRoutes[F](algebras.movies).routes(adminMiddleware)
    val adminReviewRoutes = new AdminReviewRoutes[F](algebras.reviews).routes(adminMiddleware)

    private val openRoutes : HttpRoutes[F] = movieRoutes <+> reviewRoutes
    private val adminRoutes : HttpRoutes[F] = adminMovieRoutes <+> adminReviewRoutes


    private val routes : HttpRoutes[F] = Router(
        version.v1 -> openRoutes,
        version.v1 + "/admin" -> adminRoutes
    )


    private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
        {http: HttpRoutes[F] => AutoSlash(http)} andThen
        {http: HttpRoutes[F] => CORS(http, CORS.DefaultCORSConfig)} andThen
        {http: HttpRoutes[F] => Timeout(60.seconds)(http)}
    }

    private val loggers: HttpApp[F] => HttpApp[F] = {
        {http: HttpApp[F] => RequestLogger.httpApp(true, true)(http)} andThen
        {http: HttpApp[F] => ResponseLogger.httpApp(true, true)(http)}
    }

    val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)
}

object ErrorHandler {

  import Errors._

  def apply[F[_]: MonadError[*[_], SomeError]]: HttpErrorHandler[F, SomeError] =
    new RoutesHttpErrorHandler[F, SomeError] {
      val A = implicitly

      val handler: SomeError => F[Response[F]] = {
        case _ =>
          NotFound(s"Some Error")
      }
    }
}