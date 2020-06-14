package app.vizion.exampleproject.graphqlserver.db

import app.vizion.exampleproject.graphqlserver.DbConfig
import app.vizion.exampleproject.graphqlserver.configuration.Configuration
import app.vizion.exampleProject.utils.HikariUtils
import app.vizion.exampleproject.graphqlserver.MainZ.ExampleTask
import cats.effect.{Blocker, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import zio.{Reservation, Task, ZIO, ZLayer}
import zio.Managed
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._
import com.typesafe.config.Config
import com.zaxxer.hikari.HikariDataSource

import scala.concurrent.ExecutionContext

object Persistence {
//
//  def mkTransactor(conf: com.typesafe.config.Config, connectEC: ExecutionContext, transactEc: ExecutionContext) = { //: Managed[Throwable, HikariTransactor[Task]] = {
//    val xb: Resource[ExampleTask, HikariTransactor[ExampleTask]] = for{
//      ds <- HikariUtils.createDataSourceResource[ExampleTask](conf.getConfig("hikariProperties"))
//      xa = HikariTransactor[ExampleTask](ds, connectEC, Blocker.liftExecutionContext(transactEc))
//    }yield xa
//  xb




//  def mkTransactor(conf: com.typesafe.config.Config, connectEC: ExecutionContext, transactEc: ExecutionContext) = { //: Managed[Throwable, HikariTransactor[Task]] = {
//    val xb: Resource[zio.Task, HikariTransactor[Task]] = for{
//      ds <- HikariUtils.createDataSourceResource[zio.Task](conf.getConfig("hikariProperties"))
//      xa = HikariTransactor[zio.Task](ds, connectEC, Blocker.liftExecutionContext(transactEc))
//    }yield xa
//
//    xb




//  import app.vizion.exampleproject.graphqlserver.MainZ.Task
    def mkTransactor(conf: com.typesafe.config.Config, connectEC: ExecutionContext, transactEc: ExecutionContext) = { //: Managed[Throwable, HikariTransactor[Task]] = {
      val xb: Resource[Task, HikariTransactor[Task]] = for{
        ds <- HikariUtils.createDataSourceResource[Task](conf.getConfig("hikariProperties"))
        xa = HikariTransactor[Task](ds, connectEC, Blocker.liftExecutionContext(transactEc))
      }yield xa

      xb
//
//
//
//
//    val res: ZIO[Any, Throwable, Reservation[Any, Nothing, HikariTransactor[Task]]] = xb.allocated.map{
//      case (transactor, cleanupM) => Reservation(ZIO.succeed(transactor), _ => cleanupM.orDie)
//    }.uninterruptible
//    Managed(res)
  }
}
