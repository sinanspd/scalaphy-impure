package app.vizion.exampleProject.auth

import cats.effect._
import config.data._
import dev.profunktor.redis4cats.algebra.RedisCommands
import dev.profunktor.redis4cats.connection.{ RedisClient, RedisURI }
import dev.profunktor.redis4cats.domain.RedisCodec
import dev.profunktor.redis4cats.interpreter.Redis
import dev.profunktor.redis4cats.log4cats._
import io.chrisdavenport.log4cats.Logger
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import scala.concurrent.ExecutionContext

final case class AppResources[F[_]](
    client: Client[F],
    redis: RedisCommands[F, String, String]
)

object AppResources {

  def make[F[_]: ConcurrentEffect: ContextShift: HasResourcesConfig: Logger]: Resource[F, AppResources[F]] = {

    def mkRedisResource(c: RedisConfig): Resource[F, RedisCommands[F, String, String]] =
      for {
        uri <- Resource.liftF(RedisURI.make[F](c.uri.value.value))
        client <- RedisClient[F](uri)
        cmd <- Redis[F, String, String](client, RedisCodec.Utf8, uri)
      } yield cmd

    def mkHttpClient(c: HttpClientConfig): Resource[F, Client[F]] =
      BlazeClientBuilder[F](ExecutionContext.global)
        .withConnectTimeout(c.connectTimeout)
        .withRequestTimeout(c.requestTimeout)
        .resource

    for {
      h <- Resource.liftF(F.reader(_.httpClientConfig))
      r <- Resource.liftF(F.reader(_.redis))
      client <- mkHttpClient(h)
      redis <- mkRedisResource(r)
    } yield AppResources(client, redis)
  }
}
