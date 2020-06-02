package app.vizion.exampleproject.graphqlserver

import app.vizion.exampleproject.graphqlserver.configuration.Configuration
import app.vizion.exampleproject.graphqlserver.services.BaseGraphQLService
import app.vizion.exampleproject.graphqlserver.db.Persistence
import app.vizion.exampleproject.graphqlserver.services.BaseGraphQLService.ExampleService
//import cats.effect.{Blocker, ExitCode, Sync}
//import org.http4s.implicits._
//import org.http4s.server.Router
//import org.http4s.server.blaze.BlazeServerBuilder
//import org.http4s.server.middleware.CORS
//import zio.blocking.Blocking
//import zio.clock.Clock
//import zio.console.putStrLn
//import zio.interop.catz._
//import zio._
//import caliban.Http4sAdapter
import com.typesafe.config
import com.typesafe.config.{Config, ConfigFactory}

import caliban.Http4sAdapter
import cats.data.Kleisli
import cats.effect.Blocker
import org.http4s.StaticFile
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.blocking.Blocking
import zio.console.putStrLn
import zio.interop.catz._

import scala.concurrent.ExecutionContext

object MainZ extends App {//CatsApp{

//  type AppEnvironment = Clock with Blocking
//
//  type AppTask[A] = RIO[AppEnvironment, A]

  val config = ConfigFactory.load()
  val minervaConfig = config.getConfig("app.vizion.exampleproject.api.db")

  //type ExampleTask[A] = RIO[ZEnv, A]

  type ExampleTask[A] = RIO[ZEnv with ExampleService, A]

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
//    program.foldM(
//            err => ZIO.effectTotal(err.printStackTrace()) *> putStrLn(s"Execution failed with: $err") *> IO.succeed(1),
//            _ => IO.succeed(0)
//    )
  }

//  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] ={
//    val program = for{
//      //conf <- configuration.loadConfig.provideLayer(Configuration.live) // ZIO
//      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }
//      //transactorR: Managed[Throwable, HikariTransactor[Task]] = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC)
//
//      transactorR = Persistence.mkTransactor(minervaConfig, platform.executor.asEC, blockingEC)
//      program <- transactorR.use{ xa =>
//        BaseGraphQLService
//          .make(List(), xa)
//          .memoize
//          .use(layer =>
//            for {
//              blocker     <- ZIO.access[Blocking](_.get.blockingExecutor.asEC).map(Blocker.liftExecutionContext)
//              interpreter <- new BaseGraphQLApi(xa).api.interpreter.map(_.provideCustomLayer(layer))
//              _ <- BlazeServerBuilder[ExampleTask]
//                .bindHttp(8088, "localhost")
//                .withHttpApp(
//                  Router(
//                    "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter))
//                  ).orNotFound
//                )
//                .resource
//                .toManaged
//                .useForever
//            } yield 0
//          )
//          .catchAll(err => putStrLn(err.toString).as(1))
//      }
//    }yield program
//
//
//    program
////   program.foldM(
////        err => ZIO.effectTotal(err.printStackTrace()) *> putStrLn(s"Execution failed with: $err") *> IO.succeed(1),
////        _ => IO.succeed(0)
////   )
//  }
}

case class Config(api: ApiConfig, dbConfig: DbConfig)
case class ApiConfig(endpoint: String, port: Int)
case class DbConfig(url: String, user: String, password: String)