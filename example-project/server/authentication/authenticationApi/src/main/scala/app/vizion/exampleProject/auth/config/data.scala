package app.vizion.exampleProject.auth.config

import cats.mtl.ApplicativeAsk
import ciris._
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import scala.concurrent.duration._
import com.typesafe.config.Config

object data {

  type HasAppConfig[F[_]]       = ApplicativeAsk[F, AppConfig]
  type HasResourcesConfig[F[_]] = ApplicativeAsk[F, ResourcesConfig]

  @newtype case class AdminUserTokenConfig(value: Secret[NonEmptyString])
  @newtype case class JwtSecretKeyConfig(value: Secret[NonEmptyString])
  @newtype case class JwtClaimConfig(value: Secret[NonEmptyString])
  @newtype case class TokenExpiration(value: FiniteDuration)

  @newtype case class PasswordSalt(value: Secret[NonEmptyString])

  case class AppConfig(
      adminJwtConfig: AdminJwtConfig,
      tokenConfig: JwtSecretKeyConfig,
      passwordSalt: PasswordSalt,
      tokenExpiration: TokenExpiration,
      httpServerConfig: HttpServerConfig,
      resourcesConfig: ResourcesConfig,
      transactorConfig: Config
  )

  case class ResourcesConfig(
      httpClientConfig: HttpClientConfig,
      postgreSQL: PostgreSQLConfig,
      redis: RedisConfig
  )

  case class AdminJwtConfig(
      secretKey: JwtSecretKeyConfig,
      claimStr: JwtClaimConfig,
      adminToken: AdminUserTokenConfig
  )

  case class PostgreSQLConfig(
      host: NonEmptyString,
      port: UserPortNumber,
      user: NonEmptyString,
      database: NonEmptyString,
      max: PosInt
  )

  @newtype case class RedisURI(value: NonEmptyString)
  @newtype case class RedisConfig(uri: RedisURI)

  case class HttpServerConfig(
      host: NonEmptyString,
      port: UserPortNumber
  )

  case class HttpClientConfig(
      connectTimeout: FiniteDuration,
      requestTimeout: FiniteDuration
  )

}
