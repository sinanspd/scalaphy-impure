package app.vizion.graphqlserver.configuration

import app.vizion.exampleProject.auth.config.data._

object data {
  case class AppConfig(
                        adminJwtConfig: AdminJwtConfig,
                        tokenConfig: JwtSecretKeyConfig,
                        passwordSalt: PasswordSalt,
                        tokenExpiration: TokenExpiration,
                        httpClientConfig: HttpClientConfig,
                        postgreSQL: PostgreSQLConfig,
                        redis: RedisConfig,
                        httpServerConfig: HttpServerConfig
                      )
}