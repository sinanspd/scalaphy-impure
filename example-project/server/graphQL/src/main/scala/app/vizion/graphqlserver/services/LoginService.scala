package app.vizion.exampleproject.graphqlserver.services

import com.typesafe.config.ConfigFactory
import zio._
import app.vizion.exampleProject.utils.db._
import app.vizion.exampleProject.auth.algebras.LiveAuth
import app.vizion.exampleproject.graphqlserver.MainZ.Auth
import app.vizion.exampleProject.auth.config.data.TokenExpiration
import doobie.util.transactor.Transactor
import zio.interop.catz._
import app.vizion.exampleProject.auth.config.data._
import app.vizion.exampleProject.auth.schema.auth._
import app.vizion.exampleProject.auth.algebras.{Auth => AuthT}
import dev.profunktor.auth.jwt.JwtToken

object LoginService {

  type ExampleLoginService = Has[LoginService]

  trait LoginService {
    def login(username: UserName, password: Password): RIO[Any, JwtToken]
  }

  def login(username: String, password: String): RIO[ExampleLoginService, JwtToken] =
    RIO.accessM(_.get.login(UserName(username), Password(password)))

  val config = ConfigFactory.load()
  val minervaConfig = config.getConfig("app.vizion.exampleproject.api.db")
  val minervaTransactor = transactor(minervaConfig)

  def make(security: AuthT[Task], transactor: Transactor[Task]): ZLayer[Any, Nothing, ExampleLoginService] = ZLayer.succeed {

   new LoginService {
      def login(username: UserName, password: Password): Task[JwtToken] = 
      security.login(username, password).foldM(err => ZIO.fail(err), b => ZIO.succeed(b))

    }
  }
}