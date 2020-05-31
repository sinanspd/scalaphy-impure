package app.vizion.exampleProject

import app.vizion.exampleProject.auth.AppResources
import app.vizion.exampleProject.auth.config.data._
import app.vizion.exampleProject.auth.config.data.{AppConfig, HasAppConfig}
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import cats.effect.concurrent.Ref
import org.http4s.HttpRoutes
import org.http4s.syntax._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._
import com.olegpy.meow.effects._
import com.olegpy.meow.hierarchy._
import io.chrisdavenport.log4cats.{Logger => CLogger}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import com.typesafe.scalalogging.Logger
import com.typesafe.config.{Config, ConfigFactory}
import doobie.util.transactor.Transactor
import app.vizion.exampleProject.auth._
import app.vizion.exampleProject.auth.modules.AuthModule
import app.vizion.exampleProject.modules.{Algebras, HttpApi}
import app.vizion.exampleProject.utils.db._

import scala.concurrent.ExecutionContext

object Main extends IOApp{

    implicit  val logger = Slf4jLogger.getLogger[IO]
    val cs = IO.contextShift(ExecutionContext.global)

    val configLoader: IO[Ref[IO, AppConfig]] =
     config.load[IO].flatMap(Ref.of[IO, AppConfig])

    def loadResources[F[_]: ConcurrentEffect: ContextShift: FlatMap: HasAppConfig: CLogger](
      fa: AppConfig => AppResources[F] => F[ExitCode]
    ): F[ExitCode] =
        F.ask.flatMap { cfg =>
            F.info(s"Loaded config $cfg") >>
                AppResources.make[F].use(res => fa(cfg)(res))
        }

    def run(args: List[String]): IO[ExitCode] = {
      val conf = ConfigFactory.load()
      val c = conf.getConfig("app.vizion.exampleProject.api.db")
      val tt = transactor(c)

      configLoader.flatMap(_.runAsk { implicit ioAsk =>
        loadResources[IO]{
          cfg => res =>
            tt.use { xa =>
              for{
                security <- AuthModule.make[IO](cfg, res.psql, res.redis, xa)
                algebras <- Algebras.make[IO](xa)
                api <- HttpApi.make[IO](algebras, security)
                _ <- BlazeServerBuilder[IO]
                        .bindHttp(8080, "localhost")
                        .withHttpApp(api.httpApp)
                        .serve
                        .compile
                        .drain
              } yield ExitCode.Success
          }
        }
      })
    }
}