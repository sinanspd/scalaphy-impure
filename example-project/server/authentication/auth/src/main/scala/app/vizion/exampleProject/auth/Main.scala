package app.vizion.exampleProject.auth

import cats._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import com.olegpy.meow.effects._
import com.olegpy.meow.hierarchy._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.server.blaze.BlazeServerBuilder
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext
import app.vizion.exampleProject.auth.config.data._
import app.vizion.exampleProject.auth.modules._
import app.vizion.exampleProject.auth.db._

/* Le Entry Point */
object Main extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  val cs = IO.contextShift(ExecutionContext.global)

  def loadResources[F[_]: ConcurrentEffect: ContextShift: FlatMap: HasAppConfig: Logger](
      fa: AppConfig => AppResources[F] => F[ExitCode]
  ): F[ExitCode] =
    F.ask.flatMap { cfg =>
      F.info(s"Loaded config $cfg") >>
        AppResources.make[F].use(res => fa(cfg)(res))
    }

  val configLoader: IO[Ref[IO, AppConfig]] =
    config.load[IO].flatMap(Ref.of[IO, AppConfig])

  override def run(args: List[String]): IO[ExitCode] =
    configLoader.flatMap(_.runAsk { implicit ioAsk =>
      loadResources[IO] { cfg => res =>
        transactor(cfg.transactorConfig).use { xa =>
          for {
            security <- AuthModule.make[IO](cfg, res.redis, xa)
            api <- HttpApi.make[IO](security)
            _ <- BlazeServerBuilder[IO]
                  .bindHttp(
                    cfg.httpServerConfig.port.value,
                    cfg.httpServerConfig.host.value
                  )
                  .withHttpApp(api.httpApp)
                  .serve
                  .compile
                  .drain
          } yield ExitCode.Success
        }
      }
    })
}
