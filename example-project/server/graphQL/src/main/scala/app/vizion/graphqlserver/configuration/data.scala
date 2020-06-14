package app.vizion.graphqlserver.configuration

import cats.mtl.ApplicativeAsk
import ciris._
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import scala.concurrent.duration._
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
