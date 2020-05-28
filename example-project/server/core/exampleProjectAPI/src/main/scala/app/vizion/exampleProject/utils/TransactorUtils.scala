package app.vizion.exampleProject.utils

import cats.effect._
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger

import doobie._
import doobie.hikari.HikariTransactor

object TransactorUtils {

  val log = Logger(TransactorUtils.getClass)

  def transactor(config: Config, migrationLocation: List[String])(
      implicit cs: ContextShift[IO]
  ): Resource[IO, Transactor[IO]] = {

    val poolSize         = config.getInt("poolSize")
    val hikariProperties = config.getConfig("hikariProperties")

    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](poolSize)
      be <- Blocker[IO]
      ds <- HikariUtils.createDataSourceResource[IO](hikariProperties)
      //_  <- Resource.liftF() Flyway Migeations go here
      xa = HikariTransactor[IO](ds, ce, be)
    } yield xa
  }
}
