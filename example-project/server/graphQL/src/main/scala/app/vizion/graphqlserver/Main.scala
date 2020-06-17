package app.vizion.exampleproject.graphqlserver

import com.olegpy.meow.effects._
import com.olegpy.meow.hierarchy._
import app.vizion.exampleProject.auth.config.data.{HasAppConfig, AppConfig => AAppConfig}
import app.vizion.graphqlserver.configuration.{AppResources, AuthModule, calibanExtension}
import app.vizion.exampleProject.auth.schema.auth.CommonUser
import app.vizion.exampleproject.graphqlserver.MainZ.ExampleTask
import app.vizion.exampleproject.graphqlserver.services.BaseGraphQLService
import app.vizion.exampleproject.graphqlserver.db.Persistence
import app.vizion.exampleproject.graphqlserver.services.BaseGraphQLService.ExampleService
import caliban.Http4sAdapter
import cats.{FlatMap, Foldable, MonoidK}
import cats.effect.{ConcurrentEffect, ContextShift}
import com.typesafe.config.ConfigFactory
import dev.profunktor.auth.JwtAuthMiddleware
import dev.profunktor.auth.jwt.JwtToken
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.{AuthMiddleware, Router, ServiceErrorHandler}
import org.http4s.server.middleware.CORS
import pdi.jwt.JwtClaim
import zio._
import zio.console.putStrLn
import org.http4s.server.blaze._
import io.chrisdavenport.log4cats.{Logger => CLogger}

import scala.concurrent.ExecutionContext
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import zio.interop.catz._
import io.chrisdavenport.log4cats.Logger
import cats.implicits._
import org.http4s.dsl.Http4sDsl


case class Config(api: ApiConfig, dbConfig: DbConfig)
case class ApiConfig(endpoint: String, port: Int)
case class DbConfig(url: String, user: String, password: String)

object MainZ extends App {

  val c = ConfigFactory.load()
  val minervaConfig = c.getConfig("app.vizion.exampleproject.api.db")

  implicit val logger = Slf4jLogger.getLogger[Task]

  type ExampleTask[A] = RIO[ZEnv with ExampleService, A]

  type Auth = Has[Auth.Service]
  object Auth {
    trait Service {
      def user: CommonUser
    }
  }

  import app.vizion.exampleProject.auth.{AppResources => AAppResources}
  import app.vizion.exampleProject.auth.config.data._
  def loadResources3[
    F[_]: ContextShift: FlatMap: HasAppConfig: ConcurrentEffect : CLogger : Foldable : MonoidK,
    G[_]: ContextShift: FlatMap: HasAppConfig: ConcurrentEffect : CLogger : MonoidK : Foldable
  ](fa: AAppConfig => AAppResources[F] => G[zio.ExitCode]) : F[zio.ExitCode]=
    F.ask.flatMap { cfg =>
      F.info(s"Loaded config $cfg") >>
        AAppResources.make[F].use(res => {
          fa(cfg)(res).foldMapK(_.pure[F])
        })
    }

  type ExampleTask2[A] = RIO[ZEnv with ExampleService with Auth, A]

  implicit val runtime: Runtime[ZEnv] = this

  def makeSecure(security: AuthModule[Task], routes: AuthedRoutes[CommonUser, ExampleTask]): HttpRoutes[ExampleTask] = {
    val usersAuth: JwtToken => JwtClaim => ExampleTask[Option[CommonUser]] = t => c => security.usersAuth.findUser(t)(c)
    val usersMiddleware: AuthMiddleware[ExampleTask, CommonUser] = JwtAuthMiddleware[ExampleTask, CommonUser](security.userJwtAuth.value, usersAuth)

    Router[ExampleTask](
      "/api/graphql" -> CORS(usersMiddleware(routes)),
    )
  }

  case class MissingToken() extends Throwable

  type AuthTask[A] = RIO[Auth, A]

  object dsl extends Http4sDsl[ExampleTask]
  import dsl._
  val errorHandler: ServiceErrorHandler[ExampleTask] = _ => { case MissingToken() => Forbidden() }

  object AuthMiddleware {
      import caliban.Http4sAdapter
      import dev.profunktor.auth.AuthHeaders
      import dev.profunktor.auth.jwt._
      import pdi.jwt.exceptions.JwtException
      def apply(security: AuthModule[Task], route: HttpRoutes[ExampleTask2]): HttpRoutes[ExampleTask] = {
        val usersAuth: JwtToken => JwtClaim => Task[Option[CommonUser]] = t => c => security.usersAuth.findUser(t)(c)
        val usersMiddleware: AuthMiddleware[Task, CommonUser] = JwtAuthMiddleware[Task, CommonUser](security.userJwtAuth.value, usersAuth)
        Http4sAdapter.provideSomeLayerFromRequest(
            route,
            AuthHeaders.getBearerToken(_) match {
              case Some(value) => {
                val temp =
                  jwtDecode[Task](value, security.userJwtAuth.value)
                    .flatMap(usersAuth(value))
                    .map(_.fold("not found".asLeft[CommonUser])(_.asRight[String]) match {
                      case Left(a) => Left(MissingToken())
                      case Right(a) => Right(new Auth.Service {override def user: CommonUser = a})
                    })
                    .recover{
                      case _: JwtException => Left(MissingToken())
                    }

                  temp.absolve.toLayer
              }
              case None => ZLayer.fail(MissingToken())
            }
        )
      }
    }

  override def run(args: List[String]): ZIO[ZEnv, Nothing, zio.ExitCode] ={
    val program = for{
      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }
      transactorR = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC)
      pr <- transactorR.use{ xa =>
        app.vizion.graphqlserver.configuration.load[Task].flatMap{
          cfg =>
            Logger[Task].info("Loaded conf") *>
              AppResources.make[Task](cfg).use{ res =>
                //implicitly[ConcurrentEffect[ExampleTask]]
                ZIO
                  .runtime[ZEnv with ExampleService]
                  .flatMap(implicit runtime =>
                    for {
                      security <- AuthModule.make[Task](cfg, res.redis, xa)
                      interpreter <- BaseGraphQLApi.api.interpreter
                      e =  AuthMiddleware(security, Http4sAdapter.makeHttpService(interpreter))
                      _ <- BlazeServerBuilder[ExampleTask](ExecutionContext.global)
                        .withServiceErrorHandler(errorHandler)
                        .bindHttp(8088, "localhost")
                        .withHttpApp(
                          Router[ExampleTask]("/api/graphql" -> e).orNotFound
                        )
                        .resource
                        .toManaged
                        .useForever
                    } yield zio.ExitCode.success
                  ).provideCustomLayer(BaseGraphQLService.make(List(), xa))
              }
        }
      }.catchAll(err => putStrLn(err.toString).as(zio.ExitCode.failure))
    }yield pr

    program
  }
}

//object MainZ extends App {
//
//  val c = ConfigFactory.load()
//  val minervaConfig = c.getConfig("app.vizion.exampleproject.api.db")
//
//  implicit val logger = Slf4jLogger.getLogger[Task]
//
//  type ExampleTask[A] = RIO[ZEnv with ExampleService, A]
//
//  type Auth = Has[Auth.Service]
//  object Auth {
//    trait Service {
//      def user: CommonUser
//    }
//  }
//
//  import app.vizion.exampleProject.auth.{AppResources => AAppResources}
//  import app.vizion.exampleProject.auth.config.data._
//  def loadResources3[
//    F[_]: ContextShift: FlatMap: HasAppConfig: ConcurrentEffect : CLogger : Foldable : MonoidK,
//    G[_]: ContextShift: FlatMap: HasAppConfig: ConcurrentEffect : CLogger : MonoidK : Foldable
//  ](fa: AAppConfig => AAppResources[F] => G[zio.ExitCode]) : F[zio.ExitCode]=
//    F.ask.flatMap { cfg =>
//      F.info(s"Loaded config $cfg") >>
//        AAppResources.make[F].use(res => {
//          fa(cfg)(res).foldMapK(_.pure[F])
//        })
//    }
//
//  type ExampleTask2[A] = RIO[ZEnv with ExampleService with Auth, A]
//
//  implicit val runtime: Runtime[ZEnv] = this
//
//  def makeSecure(security: AuthModule[Task], routes: AuthedRoutes[CommonUser, ExampleTask]): HttpRoutes[ExampleTask] = {
//    val usersAuth: JwtToken => JwtClaim => ExampleTask[Option[CommonUser]] = t => c => security.usersAuth.findUser(t)(c)
//    val usersMiddleware: AuthMiddleware[ExampleTask, CommonUser] = JwtAuthMiddleware[ExampleTask, CommonUser](security.userJwtAuth.value, usersAuth)
//
//    Router[ExampleTask](
//      "/api/graphql" -> CORS(usersMiddleware(routes)),
//    )
//  }
//
//  case class MissingToken() extends Throwable
//
//  type AuthTask[A] = RIO[Auth, A]
//
//  object dsl extends Http4sDsl[ExampleTask]
//  import dsl._
//  val errorHandler: ServiceErrorHandler[ExampleTask] = _ => { case MissingToken() => Forbidden() }
//
//  object AuthMiddleware {
//      import caliban.Http4sAdapter
//      import dev.profunktor.auth.AuthHeaders
//      import dev.profunktor.auth.jwt._
//      import pdi.jwt.exceptions.JwtException
//      def apply(security: AuthModule[Task], route: HttpRoutes[AuthTask]): HttpRoutes[Task] = {
//        val usersAuth: JwtToken => JwtClaim => Task[Option[CommonUser]] = t => c => security.usersAuth.findUser(t)(c)
//        val usersMiddleware: AuthMiddleware[Task, CommonUser] = JwtAuthMiddleware[Task, CommonUser](security.userJwtAuth.value, usersAuth)
//          Http4sAdapter.provideLayerFromRequest(
//          route,
//          AuthHeaders.getBearerToken(_) match {
//            case Some(value) => {
//              val temp =
//                jwtDecode[Task](value, security.userJwtAuth.value)
//                  .flatMap(usersAuth(value))
//                  .map(_.fold("not found".asLeft[CommonUser])(_.asRight[String]) match {
//                    case Left(a) => Left(MissingToken())
//                    case Right(a) => Right(new Auth.Service {override def user: CommonUser = a})
//                  })
//                  .recover{
//                    case _: JwtException => Left(MissingToken())
//                  }
//
//                temp.absolve.toLayer
//            }
//            case None => ZLayer.fail(MissingToken())
//          }
//        )
//      }
//    }
//
//  override def run(args: List[String]): ZIO[ZEnv, Nothing, zio.ExitCode] ={
//    val program = for{
//      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }
//      transactorR = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC)
//      pr <- transactorR.use{ xa =>
//        app.vizion.graphqlserver.configuration.load[Task].flatMap{
//          cfg =>
//            Logger[Task].info("Loaded conf") *>
//              AppResources.make[Task](cfg).use{ res =>
//                //implicitly[ConcurrentEffect[ExampleTask]]
//                ZIO
//                  .runtime[ZEnv with ExampleService]
//                  .flatMap(implicit runtime =>
//                    for {
//                      security <- AuthModule.make[Task](cfg, res.redis, xa)
//                      interpreter <- BaseGraphQLApi.api.interpreter
//                      //e =  AuthMiddleware(security, Http4sAdapter.makeHttpService(interpreter))
//                      _ <- BlazeServerBuilder[ExampleTask](ExecutionContext.global)
//                        .withServiceErrorHandler(errorHandler)
//                        .bindHttp(8088, "localhost")
//                        .withHttpApp(
//                          makeSecure(security, calibanExtension.makeAuthedHttpService(interpreter)).orNotFound
//                        )
//                        .resource
//                        .toManaged
//                        .useForever
//                    } yield zio.ExitCode.success
//                  ).provideCustomLayer(BaseGraphQLService.make(List(), xa))
//              }
//        }
//      }.catchAll(err => putStrLn(err.toString).as(zio.ExitCode.failure))
//    }yield pr
//
//    program
//  }
//}