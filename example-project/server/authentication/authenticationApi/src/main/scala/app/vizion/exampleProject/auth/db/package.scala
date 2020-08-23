package app.vizion.exampleProject.auth

import cats.effect.{ ContextShift, IO, Resource }
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import scala.concurrent.ExecutionContext
import app.vizion.exampleProject.auth.utils.TransactorUtils
import com.typesafe.config.Config

package object db {

  implicit val cs = IO.contextShift(ExecutionContext.global)

  def transactor(config: Config)(implicit cs: ContextShift[IO]): Resource[IO, Transactor[IO]] =
    TransactorUtils.transactor(config) //List())
}
