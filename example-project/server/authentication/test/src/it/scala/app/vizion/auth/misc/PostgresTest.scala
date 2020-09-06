package app.vizion

import cats.effect._
import cats.implicits._
import ciris._
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.ops._
import app.vizion.exampleProject.auth.utils.TransactorUtils
import doobie._
import app.vizion.exampleProject.auth.algebras.LiveUsers
import app.vizion.exampleProject.auth.algebras.LiveCrypto
import app.vizion.exampleProject.auth.config.data.PasswordSalt
import app.vizion.exampleProject.auth.schema.auth._
import app.vizion.suite.ResourceSuite 
import app.vizion.utils.arbitraries._
import com.typesafe.config.ConfigFactory

class PostgresTest extends ResourceSuite[Transactor[IO]] {

  // For it:tests, one test is enough
  val MaxTests: PropertyCheckConfigParam = MinSuccessful(1)

  lazy val salt = Secret("53kr3t": NonEmptyString).coerce[PasswordSalt]

  override def resources = TransactorUtils.transactor(ConfigFactory.load().getConfig("app.vizion.exampleProject.api.db"))

  withResources { xa => 
    forAll(MaxTests) { (username: UserName, password: Password) =>
      spec("Users") {
        for {
          c <- LiveCrypto.make[IO](salt)
          u <- LiveUsers.make[IO](c, xa)
          d <- u.create(username, password) // Can Create
          x <- u.find(username, password)   // Can Login
          y <- u.find(username, "foo".coerce[Password]) // Can't Login
          z <- u.create(username, password).attempt // Can't Double Create 
        } yield assert(
          x.count(_.id == d) == 1 && y.isEmpty && z.isLeft
        )
      }
    }
  }
}
