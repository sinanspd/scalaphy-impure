package app.vizion.exampleProject.auth.config

import cats.effect._
import cats.implicits._
import ciris._
import ciris.refined._
import environments._
import environments.AppEnvironment._
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString
import scala.concurrent.duration._
import app.vizion.exampleProject.auth.config.data._
import com.typesafe.config.ConfigFactory

object load {

  def apply[F[_]: Async: ContextShift]: F[AppConfig] =
    env("SC_APP_ENV")
      .as[AppEnvironment]
      .flatMap {
        case Test =>
          default(
            redisUri = RedisURI("redis://localhost")
          )
        case Prod =>
          default(
            redisUri = RedisURI("redis://10.123.154.176")
          )
      }
      .load[F]

  private def default(
      redisUri: RedisURI
  ): ConfigValue[AppConfig] =
    (
      env("SC_JWT_SECRET_KEY").as[NonEmptyString].secret,
      env("SC_JWT_CLAIM").as[NonEmptyString].secret,
      env("SC_ACCESS_TOKEN_SECRET_KEY").as[NonEmptyString].secret,
      env("SC_ADMIN_USER_TOKEN").as[NonEmptyString].secret,
      env("SC_PASSWORD_SALT").as[NonEmptyString].secret
    ).parMapN { (secretKey, claimStr, tokenKey, adminToken, salt) =>
      AppConfig(
        AdminJwtConfig(
          JwtSecretKeyConfig(secretKey),
          JwtClaimConfig(claimStr),
          AdminUserTokenConfig(adminToken)
        ),
        JwtSecretKeyConfig(tokenKey),
        PasswordSalt(salt),
        TokenExpiration(72.hours),
        HttpServerConfig(
          host = "0.0.0.0",
          port = 8080
        ),
        ResourcesConfig(
          HttpClientConfig(
            connectTimeout = 2.seconds,
            requestTimeout = 2.seconds
          ),
          PostgreSQLConfig(
            host = "localhost",
            port = 5432,
            user = "postgres",
            database = "auth",
            max = 10
          ),
          RedisConfig(redisUri)
        ),
        ConfigFactory.load().getConfig("app.vizion.exampleProject.api.db")
      )
    }
}
