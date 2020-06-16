package app.vizion.exampleproject.graphqlserver.db

import app.vizion.exampleProject.utils.HikariUtils
import cats.effect.{Blocker, Resource}
import doobie.hikari.HikariTransactor
import zio.Task
import zio.interop.catz._
import scala.concurrent.ExecutionContext

object Persistence {
  def mkTransactor(conf: com.typesafe.config.Config, connectEC: ExecutionContext, transactEc: ExecutionContext) = {
      val xb: Resource[Task, HikariTransactor[Task]] = for{
        ds <- HikariUtils.createDataSourceResource[Task](conf.getConfig("hikariProperties"))
        xa = HikariTransactor[Task](ds, connectEC, Blocker.liftExecutionContext(transactEc))
      }yield xa
      xb

//    val res: ZIO[Any, Throwable, Reservation[Any, Nothing, HikariTransactor[Task]]] = xb.allocated.map{
//      case (transactor, cleanupM) => Reservation(ZIO.succeed(transactor), _ => cleanupM.orDie)
//    }.uninterruptible
//    Managed(res)
  }
}