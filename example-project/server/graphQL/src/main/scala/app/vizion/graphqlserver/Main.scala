package app.vizion.exampleproject.graphqlserver

//import app.vizion.exampleProject.auth.AppResources
import java.util.UUID

import app.vizion.graphqlserver.configuration.{AppResources, AuthModule, calibanExtension}
import app.vizion.exampleProject.auth.config.data.HasAppConfig
import app.vizion.exampleProject.auth.schema.auth.{AdminUser, CommonUser, TokenNotFound}
import app.vizion.exampleproject.graphqlserver.configuration.Configuration
import app.vizion.exampleproject.graphqlserver.services.BaseGraphQLService
import app.vizion.exampleproject.graphqlserver.db.Persistence
import app.vizion.exampleproject.graphqlserver.services.BaseGraphQLService.ExampleService
import com.typesafe.config
import com.typesafe.config.{Config, ConfigFactory}
import caliban.{CalibanError, GraphQLInterpreter, Http4sAdapter}
import cats.FlatMap
import cats.data.Kleisli
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Resource, Sync}
import dev.profunktor.auth.{AuthHeaders, JwtAuthMiddleware}
import dev.profunktor.auth.jwt.{JwtToken, jwtDecode}
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.{AuthMiddleware, Router, Server}
import org.http4s.server.middleware.CORS
import pdi.jwt.JwtClaim
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.{Console, putStrLn}
import zio.interop.catz._
import io.chrisdavenport.log4cats.{Logger => CLogger}

import scala.concurrent.ExecutionContext
import app.vizion.exampleProject.auth.config.data._
import app.vizion.exampleProject.auth.config.load._
import app.vizion.exampleProject.auth._
import app.vizion.exampleProject.auth.schema.auth
import cats._
import cats.data._
import cats.implicits._
import org.http4s.syntax._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import com.olegpy.meow.effects._
import com.olegpy.meow.hierarchy._

import scala.concurrent.ExecutionContext
import cats.effect.concurrent.Ref
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.util.CaseInsensitiveString
import pdi.jwt.exceptions.JwtException
import zio.interop.catz._
import zio.interop.catz.implicits._

import ExecutionContext.Implicits.global

//object MainZ extends App {
//
//  val config = ConfigFactory.load()
//  val minervaConfig = config.getConfig("app.vizion.exampleproject.api.db")
//
//
//  type ExampleTask[A] = RIO[ZEnv with ExampleService, A]
//
//  implicit val runtime: Runtime[ZEnv] = this
//
//  implicit def unsafeLogger = Slf4jLogger.getLogger[ExampleTask]
//
//  case class MissingToken() extends Throwable
//
//    def loadResources[
//            F[_]:
//            ContextShift: FlatMap: HasAppConfig: ConcurrentEffect : CLogger
//    ](fa: AppConfig => AppResources[F] => F[zio.ExitCode]): F[zio.ExitCode] =
//        F.ask.flatMap { cfg =>
//          F.info(s"Loaded config $cfg") >>
//            AppResources.make[F].use(res => fa(cfg)(res))
//        }
//
//    val configLoader: ExampleTask[Ref[ExampleTask, AppConfig]] =
//      app.vizion.exampleProject.auth.config.load[ExampleTask].flatMap(Ref.of[ExampleTask, AppConfig])
//
//
////   object AuthMiddleware {
////     def apply(route: HttpRoutes[ExampleTask]): HttpRoutes[Task] =
////       Http4sAdapter.provideLayerFromRequest(
////         route,
////         _.headers.get(CaseInsensitiveString("token")) match {
////           case Some(value) => ZLayer.succeed(new Auth.Service { override def token: String = value.value })
////           case None        => ZLayer.fail(MissingToken())
////         }
////       )
////   }
////
//
//  override def run(args: List[String]): ZIO[ZEnv, Nothing, zio.ExitCode] ={
//    val program = for{
//      //conf <- configuration.loadConfig.provideLayer(Configuration.live) // ZIO
//      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }
//      //transactorR: Managed[Throwable, HikariTransactor[Task]] = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC)
//
//      transactorR = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC)
//      pr <- transactorR.use { xa => {
//        ZIO
//          .runtime[ZEnv with ExampleService]
//          .flatMap(implicit runtime => {
//            implicitly[ConcurrentEffect[ExampleTask]]
//              configLoader.flatMap(_.runAsk { implicit ioAsk =>
//                loadResources[ExampleTask] {
//                  cfg =>
//                    res =>
//                      for {
//                        blocker <- ZIO.access[Blocking](_.get.blockingExecutor.asEC).map(Blocker.liftExecutionContext)
//                        security <- AuthModule.make[ExampleTask](cfg, res.redis, xa)
//                        interpreter <- new BaseGraphQLApi(xa).api.interpreter
//                        x =  BlazeServerBuilder[ExampleTask](ExecutionContext.global)
//                          .bindHttp(8088, "localhost")
//                          .withHttpApp(
//                            Router[ExampleTask](
//                              "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
//                            ).orNotFound
//                          )
//                          .resource
//                          .toManaged
//                          .useForever
//                      } yield zio.ExitCode.success
//                }})
//          }).provideCustomLayer(BaseGraphQLService.make(List(), xa))
//      }}.catchAll(err => putStrLn(err.toString).as(zio.ExitCode.failure))
//    }yield pr
//
//
//   program
//  }
//
//
//}

case class Config(api: ApiConfig, dbConfig: DbConfig)
case class ApiConfig(endpoint: String, port: Int)
case class DbConfig(url: String, user: String, password: String)



object MainZ extends App {
  import io.chrisdavenport.log4cats.Logger


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

//    object AuthMiddleware {
//
//      def apply(security: AuthModule[Task], route: HttpRoutes[AuthTask]): HttpRoutes[Task] = {
//        val usersAuth: JwtToken => JwtClaim => Task[Option[CommonUser]] = t => c => security.usersAuth.findUser(t)(c)
//        val usersMiddleware: AuthMiddleware[Task, CommonUser] = JwtAuthMiddleware[Task, CommonUser](security.userJwtAuth.value, usersAuth)
//
//        Http4sAdapter.provideLayerFromRequest(
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

  override def run(args: List[String]): ZIO[ZEnv, Nothing, zio.ExitCode] ={
    val program = for{
      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }
      transactorR = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC)
      pr <- transactorR.use{ xa =>
        app.vizion.graphqlserver.configuration.load[Task].flatMap{
          cfg =>
            Logger[Task].info("Loaded conf") *>
              AppResources.make[Task](cfg).use{ res =>
                ZIO
                  .runtime[ZEnv with ExampleService]
                  .flatMap(implicit runtime =>
                    for {
                      // blocker     <- ZIO.access[Blocking](_.get.blockingExecutor.asEC).map(Blocker.liftExecutionContext)
                      security <- AuthModule.make[Task](cfg, res.redis, xa)
                      interpreter <- BaseGraphQLApi.api.interpreter
                      //e =  AuthMiddleware(security, Http4sAdapter.makeHttpService(interpreter))
                      _ <- BlazeServerBuilder[ExampleTask](ExecutionContext.global)
                        .bindHttp(8088, "localhost")
                        .withHttpApp(
                          makeSecure(security, calibanExtension.makeAuthedHttpService(interpreter)).orNotFound
//                          Router[ExampleTask](
//                            "/api/graphql" -> //CORS(Http4sAdapter.makeHttpService(interpreter)),
//                          ).orNotFound
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































// Compiles, loads the config but no AUTH
//object MainZ extends App {
//  import io.chrisdavenport.log4cats.Logger
//
//
//  val c = ConfigFactory.load()
//  val minervaConfig = c.getConfig("app.vizion.exampleproject.api.db")
//
//  implicit val logger = Slf4jLogger.getLogger[Task]
//
//  type ExampleTask[A] = RIO[ZEnv with ExampleService, A]
//
//  implicit val runtime: Runtime[ZEnv] = this
//
//
//  def makeSecure(security: AuthModule[Task], routes: AuthedRoutes[CommonUser, ExampleTask]): HttpRoutes[ExampleTask] = {
//    val usersAuth: JwtToken => JwtClaim => ExampleTask[Option[CommonUser]] = t => c => security.usersAuth.findUser(t)(c)
//    val usersMiddleware: AuthMiddleware[ExampleTask, CommonUser] = JwtAuthMiddleware[ExampleTask, CommonUser](security.userJwtAuth.value, usersAuth)
//
//    Router[ExampleTask](
//      "/api/graphql" -> usersMiddleware(routes),
//    )
//  }
//
////  object AuthMiddleware {
////       def apply(route: HttpRoutes[ExampleTask]): HttpRoutes[Task] =
////         Http4sAdapter.provideLayerFromRequest(
////           route,
////           _.headers.get(CaseInsensitiveString("token")) match {
////             case Some(value) => ZLayer.succeed(new Auth.Service { override def token: String = value.value })
////             case None        => ZLayer.fail(MissingToken())
////           }
////         )
////  }
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
//                ZIO
//                  .runtime[ZEnv with ExampleService]
//                  .flatMap(implicit runtime =>
//                    for {
//                      // blocker     <- ZIO.access[Blocking](_.get.blockingExecutor.asEC).map(Blocker.liftExecutionContext)
//                      security <- AuthModule.make[Task](cfg, res.redis, xa)
//                      interpreter <- BaseGraphQLApi.api.interpreter
//                     // e = makeSecure(security, Http4sAdapter.makeAu
//                      _ <- BlazeServerBuilder[ExampleTask](ExecutionContext.global)
//                        .bindHttp(8088, "localhost")
//                        .withHttpApp(
//                          Router[ExampleTask](
//                            "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
//                          ).orNotFound
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


































//
//object MainZ extends App {
//
//  val config = ConfigFactory.load()
//  val minervaConfig = config.getConfig("app.vizion.exampleproject.api.db")
//
//
//  type ExampleTask[A] = RIO[ZEnv with ExampleService, A]
//  //type Task[A] = RIO[ZEnv, A]
//
//  implicit val runtime: Runtime[ZEnv] = this
//
//  implicit def unsafeLogger = Slf4jLogger.getLogger[ExampleTask]
//
//  implicit def unsafeLogger2 = Slf4jLogger.getLogger[Task]
//
//  override def run(args: List[String]): ZIO[ZEnv, Nothing, zio.ExitCode] = {
//    val program = for {
//      blockingEC <- blocking.blocking {
//        ZIO.descriptor.map(_.executor.asEC)
//      }
//      transactorR = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC).toManaged
//      pr <- transactorR.use { xa => {
//        ZIO //ZIO[zio.ZEnv with ExampleService, Throwable, ExitCode]  || ZIO[zio.ZEnv, Throwable, ExitCode]
//          .runtime[ZEnv with ExampleService]
//          .flatMap(implicit runtime => {
//            implicitly[ConcurrentEffect[ExampleTask]]
//            configLoader.flatMap(_.runAsk { implicit ioAsk =>
//              loadResources[Task] { // The problem occurs here since F[ExitCode] where F = Task ~ ZIO[Any, Nothing, ExitCode]
//                cfg =>
//                  res =>
//                    for {
//                      security <- AuthModule.make[Task](cfg, res.redis, xa)
//                      interpreter <- BaseGraphQLApi.api.interpreter
//                      _ <- BlazeServerBuilder[ExampleTask](ExecutionContext.global)
//                        .bindHttp(8088, "localhost")
//                        .withHttpApp(
//                          Router[ExampleTask](
//                            "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
//                          ).orNotFound
//                        )
//                        .resource
//                        .toManaged
//                        .useForever
//                      //.provideCustomLayer(BaseGraphQLService.make(List(), xa))
//                    } yield zio.ExitCode.success
//              }
//            })
//          }).provideCustomLayer(BaseGraphQLService.make(List(), xa))
//      }
//      }.catchAll(err => putStrLn(err.toString).as(zio.ExitCode.failure))
//    } yield pr
//    program
//  }
//}
//
//
//















// object MainZ extends App {
//
//  val config = ConfigFactory.load()
//  val minervaConfig = config.getConfig("app.vizion.exampleproject.api.db")
//  implicit val cs = cats.effect.IO.contextShift(ExecutionContext.global)
//
//  def loadResources[F[_]: ConcurrentEffect: ContextShift: FlatMap: HasAppConfig: CLogger](
//                                                                                           fa: AppConfig => AppResources[F] => F[ExitCode]
//                                                                                         ): F[ExitCode] =
//    F.ask.flatMap { cfg =>
//      F.info(s"Loaded config $cfg") >>
//        AppResources.make[F].use(res => fa(cfg)(res))
//    }
//
//  val configLoader: cats.effect.IO[Ref[cats.effect.IO, AppConfig]] =
//    app.vizion.exampleProject.auth.config.load[cats.effect.IO].flatMap(Ref.of[cats.effect.IO, AppConfig])
//
//
//  case class MissingToken() extends Throwable
//
//  type ExampleTask[A] = RIO[ZEnv with ExampleService, A]
//
//
//
//  override def run(args: List[String]): ZIO[ZEnv, Nothing, zio.ExitCode] ={
//    val program = for{
//      //conf <- configuration.loadConfig.provideLayer(Configuration.live) // ZIO
//      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }
//      //transactorR: Managed[Throwable, HikariTransactor[Task]] = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC)
//
//      transactorR = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC)
//      pr <- transactorR.use{ xa =>
//        ZIO
//          .runtime[ZEnv with ExampleService]
//          .flatMap(implicit runtime =>
//            for {
//             // blocker     <- ZIO.access[Blocking](_.get.blockingExecutor.asEC).map(Blocker.liftExecutionContext)
//              interpreter <- BaseGraphQLApi.api.interpreter
//              x =  BlazeServerBuilder[ExampleTask](ExecutionContext.global)
//                .bindHttp(8088, "localhost")
//                .withHttpApp(
//                  Router[ExampleTask](
//                    "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
//                  ).orNotFound
//                )
//                .resource
//                .toManaged
//                .useForever
//            } yield zio.ExitCode.success
//          )
//          .provideCustomLayer(BaseGraphQLService.make(List(), xa))
//      }.catchAll(err => putStrLn(err.toString).as(zio.ExitCode.failure))
//    }yield pr
//
//
//    program
//  }
// }

//
//object MainZ extends App {
//
//  val config = ConfigFactory.load()
//  val minervaConfig = config.getConfig("app.vizion.exampleproject.api.db")
//
//
//  type ExampleTask[A] = RIO[ZEnv with ExampleService, A]
//  //type Task[A] = RIO[ZEnv, A]
//
//  implicit val runtime: Runtime[ZEnv] = this
//
//  implicit def unsafeLogger = Slf4jLogger.getLogger[ExampleTask]
//  implicit def unsafeLogger2 = Slf4jLogger.getLogger[Task]
//
//  case class MissingToken() extends Throwable
//
//    def loadResources[
//            F[_]: ContextShift: FlatMap: HasAppConfig: ConcurrentEffect : CLogger
//    ](fa: AppConfig => AppResources[F] => F[zio.ExitCode]) : F[zio.ExitCode]=
//        F.ask.flatMap { cfg =>
//          F.info(s"Loaded config $cfg") >>
//            AppResources.make[F].use(res => {
//                fa(cfg)(res)
//              }
//            )
//        }
//
//  def loadResources3[
//    F[_]: ContextShift: FlatMap: HasAppConfig: ConcurrentEffect : CLogger : Foldable : MonoidK,
//    G[_]: ContextShift: FlatMap: HasAppConfig: ConcurrentEffect : CLogger : MonoidK : Foldable
//    ](fa: AppConfig => AppResources[F] => G[zio.ExitCode]) : F[zio.ExitCode]=
//      F.ask.flatMap { cfg =>
//        F.info(s"Loaded config $cfg") >>
//          AppResources.make[F].use(res => {
//            fa(cfg)(res).foldMapK(_.pure[F])
//          }
//          )
//      }
//
//  def loadResources2[
//        F[_] : ContextShift: FlatMap: HasAppConfig: ConcurrentEffect : CLogger : Foldable
//      ](fa: AppConfig => AppResources[F] => F[ExampleTask[zio.ExitCode]]) : F[ExampleTask[zio.ExitCode]]= //: F[zio.ExitCode] =
//        F.ask.flatMap { cfg =>
//            AppResources.make[F].use((res: AppResources[F]) => {
//                fa(cfg)(res)
//              }
//            )
//        }
//
//    val configLoader: Task[Ref[Task, AppConfig]] =
//      app.vizion.exampleProject.auth.config.load[Task].flatMap(Ref.of[Task, AppConfig])
//
//
//  override def run(args: List[String]): ZIO[ZEnv, Nothing, zio.ExitCode] ={
//    val program = for{
//      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }
//      transactorR = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC).toManaged
//      pr <- transactorR.use { xa  => {
//        ZIO //ZIO[zio.ZEnv with ExampleService, Throwable, ExitCode]  || ZIO[zio.ZEnv, Throwable, ExitCode]
//          .runtime[ZEnv with ExampleService]
//          .flatMap(implicit runtime => {
//            implicitly[ConcurrentEffect[ExampleTask]]
//            configLoader.flatMap(_.runAsk { implicit ioAsk =>
//              loadResources[Task] { // The problem occurs here since F[ExitCode] where F = Task ~ ZIO[Any, Nothing, ExitCode]
//                cfg =>
//                  res =>
//                    for {
//                      security <- AuthModule.make[Task](cfg, res.redis, xa)
//                      interpreter <- BaseGraphQLApi.api.interpreter
//                      _ <-  BlazeServerBuilder[ExampleTask](ExecutionContext.global)
//                        .bindHttp(8088, "localhost")
//                        .withHttpApp(
//                          Router[ExampleTask](
//                            "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
//                          ).orNotFound
//                        )
//                        .resource
//                        .toManaged
//                        .useForever
//                      //.provideCustomLayer(BaseGraphQLService.make(List(), xa))
//                    } yield zio.ExitCode.success
//              }})
//          }).provideCustomLayer(BaseGraphQLService.make(List(), xa))
//      }}.catchAll(err => putStrLn(err.toString).as(zio.ExitCode.failure))
//    }yield pr
//    program
//  }



//    override def run(args: List[String]): ZIO[ZEnv, Nothing, zio.ExitCode] ={
//      val program = for{
//        blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }
//        transactorR = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC).toManaged
//        pr <- transactorR.use { xa  => {
//          ZIO //ZIO[zio.ZEnv with ExampleService, Throwable, ExitCode]  || ZIO[zio.ZEnv, Throwable, ExitCode]
//            .runtime[ZEnv with ExampleService]
//            .flatMap(implicit runtime => {
//              implicitly[ConcurrentEffect[ExampleTask]]
//                configLoader.flatMap(_.runAsk { implicit ioAsk =>
//                  loadResources[Task] { // The problem occurs here since F[ExitCode] where F = Task ~ ZIO[Any, Nothing, ExitCode]
//                    cfg =>
//                      res =>
//                        for {
//                          security <- AuthModule.make[Task](cfg, res.redis, xa)
//                          interpreter <- BaseGraphQLApi.api.interpreter
//                          _ <-  BlazeServerBuilder[ExampleTask](ExecutionContext.global)
//                            .bindHttp(8088, "localhost")
//                            .withHttpApp(
//                              Router[ExampleTask](
//                                "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
//                              ).orNotFound
//                            )
//                            .resource
//                            .toManaged
//                            .useForever
//                            //.provideCustomLayer(BaseGraphQLService.make(List(), xa))
//                        } yield zio.ExitCode.success
//                  }})
//            }).provideCustomLayer(BaseGraphQLService.make(List(), xa))
//        }}.catchAll(err => putStrLn(err.toString).as(zio.ExitCode.failure))
//      }yield pr
//     program
//    }


//
//  override def run(args: List[String]): ZIO[ZEnv, Nothing, zio.ExitCode] ={
//    val program = for{
//      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }
//      transactorR = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC).toManaged
//      pr <- transactorR.use { xa  => {
//        ZIO //ZIO[zio.ZEnv with ExampleService, Throwable, ExitCode]  || ZIO[zio.ZEnv, Throwable, ExitCode]
//          .runtime[ZEnv with ExampleService]
//          .flatMap(implicit runtime => {
//            implicitly[ConcurrentEffect[ExampleTask]]
//              configLoader.flatMap(_.runAsk { implicit ioAsk =>
//                loadResources[Task] { // The problem occurs here since F[ExitCode] where F = Task ~ ZIO[Any, Nothing, ExitCode]
//                  cfg =>
//                    res =>
//                      for {
//                        security <- AuthModule.make[Task](cfg, res.redis, xa)
//                        interpreter <- BaseGraphQLApi.api.interpreter
//                        _ <-  BlazeServerBuilder[ExampleTask](ExecutionContext.global)
//                          .bindHttp(8088, "localhost")
//                          .withHttpApp(
//                            Router[ExampleTask](
//                              "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
//                            ).orNotFound
//                          )
//                          .resource
//                          .toManaged
//                          .useForever
//                          //.provideCustomLayer(BaseGraphQLService.make(List(), xa))
//                      } yield zio.ExitCode.success
//                }})
//          }).provideCustomLayer(BaseGraphQLService.make(List(), xa))
//      }}.catchAll(err => putStrLn(err.toString).as(zio.ExitCode.failure))
//    }yield pr
//   program
//  }


//}
