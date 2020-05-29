package app.vizion.exampleProject.auth.modules

import cats.effect._
import cats.implicits._
import com.olegpy.meow.hierarchy._
import dev.profunktor.auth.JwtAuthMiddleware
import dev.profunktor.auth.jwt.JwtToken
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware._
import org.http4s.server.Router
import pdi.jwt._
import scala.concurrent.duration._
import app.vizion.exampleProject.auth.schema.auth._
import app.vizion.exampleProject.auth.http.routes._

object HttpApi {
  def make[F[_]: Concurrent: Timer](
      security: AuthModule[F]
  ): F[HttpApi[F]] =
    Sync[F].delay(
      new HttpApi[F](
        security
      )
    )
}

final class HttpApi[F[_]: Concurrent: Timer] private (
    security: AuthModule[F]
) {
//  private val adminAuth: JwtToken => JwtClaim => F[Option[AdminUser]] =
//    t => c => security.adminAuth.findUser(t)(c)
  private val usersAuth: JwtToken => JwtClaim => F[Option[CommonUser]] =
    t => c => security.usersAuth.findUser(t)(c)

  //private val adminMiddleware = JwtAuthMiddleware[F, AdminUser](security.adminJwtAuth.value, adminAuth)
  private val usersMiddleware = JwtAuthMiddleware[F, CommonUser](security.userJwtAuth.value, usersAuth)

  // Auth routes
  private val loginRoutes  = new LoginRoutes[F](security.auth).routes
  private val logoutRoutes = new LogoutRoutes[F](security.auth).routes(usersMiddleware)
  private val userRoutes   = new UserRoutes[F](security.auth).routes

  // Combining all the http routes
  private val openRoutes: HttpRoutes[F] = loginRoutes <+> userRoutes <+> logoutRoutes

  // private val adminRoutes: HttpRoutes[F] =

  private val routes: HttpRoutes[F] = Router(
    version.v1 -> openRoutes
    //version.v1 + "/admin" -> adminRoutes
  )

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS(http, CORS.DefaultCORSConfig)
    } andThen { http: HttpRoutes[F] =>
      Timeout(60.seconds)(http)
    }
  }

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(true, true)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(true, true)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

}
