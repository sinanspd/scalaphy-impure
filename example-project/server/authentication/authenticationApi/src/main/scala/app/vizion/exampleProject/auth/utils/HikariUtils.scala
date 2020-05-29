package app.vizion.exampleProject.auth.utils

import cats.effect.{ Resource, Sync }
import com.typesafe.config.Config
import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }

object HikariUtils {

  def config(conf: Config): HikariConfig =
    new HikariConfig(TypesafeUtils.toProperties(conf))

  def createDataSourceResource[M[_]: Sync](config: Config): Resource[M, HikariDataSource] = {
    val alloc = Sync[M].delay(new HikariDataSource(this.config(config)))
    val free  = (ds: HikariDataSource) => Sync[M].delay(ds.close())
    Resource.make(alloc)(free)
  }

  def unsafeCreateDataSource(config: Config): HikariDataSource =
    new HikariDataSource(this.config(config))
}
