package app.vizion.exampleProject.auth.modules

import cats.effect._
import cats.implicits._
import dev.profunktor.auth.jwt._
import dev.profunktor.redis4cats.algebra.RedisCommands
import pdi.jwt._
import app.vizion.exampleProject.auth.algebras._
import app.vizion.exampleProject.auth.config.data._
import app.vizion.exampleProject.auth.schema.auth._
import doobie.util.transactor.Transactor
import skunk.Session

object AuthModule {
  def make[F[_]: Sync](
      cfg: AppConfig,
      sessionPool: Resource[F, Session[F]],
      redis: RedisCommands[F, String, String],
      xa: Transactor[F]
  ): F[AuthModule[F]] = {

    val adminJwtAuth: AdminJwtAuth =
      AdminJwtAuth(
        JwtAuth
          .hmac(
            cfg.adminJwtConfig.secretKey.value.value.value,
            JwtAlgorithm.HS256
          )
      )

    val userJwtAuth: UserJwtAuth =
      UserJwtAuth(
        JwtAuth
          .hmac(
            cfg.tokenConfig.value.value.value,
            JwtAlgorithm.HS256
          )
      )

    val adminToken = JwtToken(cfg.adminJwtConfig.adminToken.value.value.value)

    for {
      adminClaim <- jwtDecode[F](adminToken, adminJwtAuth.value)
      content = adminClaim.content.replace("{", "0").replace("}", "c")
      adminId <- GenUUID[F].read[UserId](content)
      adminUser = AdminUser(User(adminId, UserName("admin")))
      tokens <- LiveTokens.make[F](cfg.tokenConfig, cfg.tokenExpiration)
      crypto <- LiveCrypto.make[F](cfg.passwordSalt)
      users <- LiveUsers.make[F](sessionPool, crypto)
      auth <- LiveAuth.make[F](cfg.tokenExpiration, tokens, users, redis, xa)
      adminAuth <- LiveAdminAuth.make[F](adminToken, adminUser)
      usersAuth <- LiveUsersAuth.make[F](redis)
    } yield new AuthModule[F](auth, adminAuth, usersAuth, adminJwtAuth, userJwtAuth)

  }
}

final class AuthModule[F[_]] private (
    val auth: Auth[F],
    val adminAuth: UsersAuth[F, AdminUser],
    val usersAuth: UsersAuth[F, CommonUser],
    val adminJwtAuth: AdminJwtAuth,
    val userJwtAuth: UserJwtAuth
) {}
