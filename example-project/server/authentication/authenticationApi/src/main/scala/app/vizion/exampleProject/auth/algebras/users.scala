package app.vizion.exampleProject.auth.algebras

import cats.effect._
import cats.implicits._
import app.vizion.exampleProject.auth.schema.auth._
import app.vizion.exampleProject.auth.effects._
//import app.vizion.exampleProject.auth.ext.skunkx._
import doobie.util.transactor.Transactor
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import app.vizion.exampleProject.auth.utils.json._
//import skunk._
//import skunk.codec.all._
//import skunk.implicits._
import java.util.UUID

trait Users[F[_]] {
  def find(username: UserName, password: Password): F[Option[User]]
  def create(username: UserName, password: Password): F[UserId]
}

object LiveUsers {
  def make[F[_]: Sync](
      //sessionPool: Resource[F, Session[F]],
      crypto: Crypto,
      xa: Transactor[F]
  ): F[Users[F]] =
    Sync[F].delay(
      new LiveUsers[F](crypto, xa)
    )
}

final class LiveUsers[F[_]: BracketThrow: GenUUID] private (
    //sessionPool: Resource[F, Session[F]],
    crypto: Crypto,
    xa: Transactor[F]
) extends Users[F] {
  import UserQueries._

  def find(username: UserName, password: Password): F[Option[User]] = {
    val query = sql"SELECT * FROM users WHERE name = ${username.value} "
      .query[User]
      .to[List]

    query.transact(xa).map(_.headOption)
  }

  def create(username: UserName, password: Password): F[UserId] =
    GenUUID[F].make[UserId].flatMap { id =>
      val query: ConnectionIO[String] =
        sql"INSERT INTO users (uuid, name, password) VALUES ((${id})::uuid, ${username.value}, ${password.value})".update
          .withUniqueGeneratedKeys("uuid")
//              .handleErrorWith {
//                case SqlState.UniqueViolation(_) =>
//                  UserNameInUse(username).raiseError[F, UserId]
//              }

      query.transact(xa).map((s: String) => UserId(UUID.fromString(s)))
    }

//  def find(username: UserName, password: Password): F[Option[User]] =
//    sessionPool.use { session =>
//      session.prepare(selectUser).use { q =>
//        q.option(username).map {
//          case Some(u ~ p) if p.value == crypto.encrypt(password).value => u.some
//          case _                                                        => none[User]
//        }
//      }
//    }
//
//  def create(username: UserName, password: Password): F[UserId] =
//    sessionPool.use { session =>
//      session.prepare(insertUser).use { cmd =>
//        GenUUID[F].make[UserId].flatMap { id =>
//          cmd
//            .execute(User(id, username) ~ crypto.encrypt(password))
//            .as(id)
//            .handleErrorWith {
//              case SqlState.UniqueViolation(_) =>
//                UserNameInUse(username).raiseError[F, UserId]
//            }
//        }
//      }
//    }

}

private object UserQueries {

//  val codec: Codec[User ~ EncryptedPassword] =
//    (uuid.cimap[UserId] ~ varchar.cimap[UserName] ~ varchar.cimap[EncryptedPassword]).imap {
//      case i ~ n ~ p =>
//        User(i, n) ~ p
//    } {
//      case u ~ p =>
//        u.id ~ u.name ~ p
//    }
//
//  val selectUser: Query[UserName, User ~ EncryptedPassword] =
//    sql"""
//        SELECT * FROM users
//        WHERE name = ${varchar.cimap[UserName]}
//       """.query(codec)
//
//  val insertUser: Command[User ~ EncryptedPassword] =
//    sql"""
//        INSERT INTO users
//        VALUES ($codec)
//        """.command

}
