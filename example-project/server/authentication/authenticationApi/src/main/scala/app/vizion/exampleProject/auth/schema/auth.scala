package app.vizion.exampleProject.auth.schema

import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import java.util.UUID
import javax.crypto.Cipher
import scala.util.control.NoStackTrace
import dev.profunktor.auth.jwt._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import doobie.refined.implicits._
import doobie.Read
import doobie.Put
import eu.timepit.refined.api.Refined
import doobie.implicits._
import cats.Eq
import cats._
import cats.data.OptionT
import cats.implicits._
import doobie.postgres.implicits._

object auth {

  implicit def newTypePut[N: Coercible[R, *], R: Put]: Put[N] = Put[R].contramap[N](_.repr.asInstanceOf[R])

  implicit def newTypeRead[N: Coercible[R, *], R: Read]: Read[N] = Read[R].map(_.asInstanceOf[N])

  /** If we have an Eq instance for Repr type R, derive an Eq instance for  NewType N. */
  implicit def coercibleEq[R, N](implicit ev: Coercible[Eq[R], Eq[N]], R: Eq[R]): Eq[N] =
    ev(R)

  @newtype case class UserId(value: UUID)
  @newtype case class UserName(value: String)
  @newtype case class Password(value: String)

  @newtype case class EncryptedPassword(value: String)

  @newtype case class EncryptCipher(value: Cipher)
  @newtype case class DecryptCipher(value: Cipher)

  @newtype case class AdminJwtAuth(value: JwtSymmetricAuth)
  @newtype case class UserJwtAuth(value: JwtSymmetricAuth)

  case class User(id: UserId, name: UserName)

  @newtype case class CommonUser(value: User)
  @newtype case class AdminUser(value: User)

  // --------- user registration -----------

  @newtype case class UserNameParam(value: NonEmptyString) {
    def toDomain: UserName = UserName(value.value.toLowerCase)
  }

  @newtype case class PasswordParam(value: NonEmptyString) {
    def toDomain: Password = Password(value.value)
  }

  case class CreateUser(
      username: UserNameParam,
      password: PasswordParam
  )

  case class UserNameInUse(username: UserName) extends NoStackTrace
  case class InvalidUserOrPassword(username: UserName) extends NoStackTrace
  case object UnsupportedOperation extends NoStackTrace

  case object TokenNotFound extends NoStackTrace

  // --------- user login -----------

  case class LoginUser(
      username: UserNameParam,
      password: PasswordParam
  )
}
