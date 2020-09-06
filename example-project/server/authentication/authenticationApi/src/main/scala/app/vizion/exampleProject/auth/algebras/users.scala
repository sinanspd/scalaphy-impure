package app.vizion.exampleProject.auth.algebras

import cats.effect._
import cats.implicits._
import app.vizion.exampleProject.auth.schema.auth._
import app.vizion.exampleProject.auth.effects._
import doobie.util.transactor.Transactor
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import java.util.UUID

trait Users[F[_]] {
  def find(username: UserName, password: Password): F[Option[User]]
  def create(username: UserName, password: Password): F[UserId]
}

object LiveUsers {
  def make[F[_]: Sync](
      crypto: Crypto,
      xa: Transactor[F]
  ): F[Users[F]] =
    Sync[F].delay(
      new LiveUsers[F](crypto, xa)
    )
}

final class LiveUsers[F[_]: BracketThrow: GenUUID] private (
    crypto: Crypto,
    xa: Transactor[F]
) extends Users[F] {

  type UserPasswordPair = (UserId, UserName, Password)

  def find(username: UserName, password: Password): F[Option[User]] = {
    val query = sql"SELECT uuid, name, password  FROM users WHERE name = ${username.value} "
      .query[UserPasswordPair]
      .to[List]

    query.transact(xa).map {
      _.headOption match {
        case Some(u) if u._3 == crypto.encrypt(password).value => Some(User(u._1, u._2))
        case _                                                 => None
      }
    }
  }

  def create(username: UserName, password: Password): F[UserId] =
    GenUUID[F].make[UserId].flatMap { id =>
      val ecrpy = crypto.encrypt(password)
      val query: ConnectionIO[String] =
        sql"INSERT INTO users (uuid, name, password) VALUES ((${id})::uuid, ${username.value}, ${ecrpy.value})".update
          .withUniqueGeneratedKeys("uuid")
//              .handleErrorWith {
//                case SqlState.UniqueViolation(_) =>
//                  UserNameInUse(username).raiseError[F, UserId]
//              }

      query.transact(xa).map((s: String) => UserId(UUID.fromString(s)))
    }
}
