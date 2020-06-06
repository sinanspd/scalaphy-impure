package app.vizion.exampleproject.graphqlserver

import app.vizion.exampleProject.auth.AppResources
import app.vizion.exampleProject.auth.config.data.HasAppConfig
import app.vizion.exampleProject.auth.schema.auth.{AdminUser, CommonUser}
import app.vizion.exampleproject.graphqlserver.configuration.Configuration
import app.vizion.exampleproject.graphqlserver.services.BaseGraphQLService
import app.vizion.exampleproject.graphqlserver.db.Persistence
import app.vizion.exampleproject.graphqlserver.services.BaseGraphQLService.ExampleService
import com.typesafe.config
import com.typesafe.config.{Config, ConfigFactory}
import caliban.{CalibanError, GraphQLInterpreter, Http4sAdapter}
import cats.FlatMap
import cats.data.Kleisli
import cats.effect.{Blocker, ConcurrentEffect, ContextShift}
import dev.profunktor.auth.JwtAuthMiddleware
import dev.profunktor.auth.jwt.JwtToken
import org.http4s.StaticFile
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
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
import cats._
import cats.data._
import cats.implicits._

import org.http4s.HttpRoutes
import org.http4s.syntax._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._
import com.olegpy.meow.effects._
import com.olegpy.meow.hierarchy._
import scala.concurrent.ExecutionContext
import cats.effect.concurrent.Ref




object MainZ extends App {

  val config = ConfigFactory.load()
  val minervaConfig = config.getConfig("app.vizion.exampleproject.api.db")
  implicit val cs = cats.effect.IO.contextShift(ExecutionContext.global)

  def loadResources[F[_]: ConcurrentEffect: ContextShift: FlatMap: HasAppConfig: CLogger](
                                                                                           fa: AppConfig => AppResources[F] => F[ExitCode]
                                                                                         ): F[ExitCode] =
    F.ask.flatMap { cfg =>
      F.info(s"Loaded config $cfg") >>
        AppResources.make[F].use(res => fa(cfg)(res))
    }

  val configLoader: cats.effect.IO[Ref[cats.effect.IO, AppConfig]] =
    app.vizion.exampleProject.auth.config.load[cats.effect.IO].flatMap(Ref.of[cats.effect.IO, AppConfig])


  case class MissingToken() extends Throwable

  type ExampleTask[A] = RIO[ZEnv with ExampleService, A]


  // object AuthMiddleware {
  //   def apply(route: HttpRoutes[ExampleTask]): HttpRoutes[Task] =
  //     Http4sAdapter.provideLayerFromRequest(
  //       route,
  //       _.headers.get(CaseInsensitiveString("token")) match {
  //         case Some(value) => ZLayer.succeed(new Auth.Service { override def token: String = value.value })
  //         case None        => ZLayer.fail(MissingToken())
  //       }
  //     )
  // }

  override def run(args: List[String]): ZIO[ZEnv, Nothing, zio.ExitCode] ={
    val program = for{
      //conf <- configuration.loadConfig.provideLayer(Configuration.live) // ZIO
      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }
      //transactorR: Managed[Throwable, HikariTransactor[Task]] = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC)

      transactorR = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC)
      pr <- transactorR.use{ xa =>
        ZIO
            .runtime[ZEnv with ExampleService]
            .flatMap(implicit runtime =>
              for {
                blocker     <- ZIO.access[Blocking](_.get.blockingExecutor.asEC).map(Blocker.liftExecutionContext)
                interpreter <- new BaseGraphQLApi(xa).api.interpreter
                _ <- BlazeServerBuilder[ExampleTask](ExecutionContext.global)
                      .bindHttp(8088, "localhost")
                      .withHttpApp(
                        Router[ExampleTask](
                          "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
                        ).orNotFound
                      )
                      .resource
                      .toManaged
                      .useForever
            } yield zio.ExitCode.success
          )
          .provideCustomLayer(BaseGraphQLService.make(List(), xa))
      }.catchAll(err => putStrLn(err.toString).as(zio.ExitCode.failure))
    }yield pr


   program
  }
}

case class Config(api: ApiConfig, dbConfig: DbConfig)
case class ApiConfig(endpoint: String, port: Int)
case class DbConfig(url: String, user: String, password: String)