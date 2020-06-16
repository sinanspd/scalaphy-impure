package app.vizion.exampleproject.graphqlserver.configuration

import app.vizion.exampleproject.graphqlserver.Config
import zio.{Layer, Task, ZLayer}
import pureconfig.ConfigSource.default.loadOrThrow

object Configuration {
    trait Service{
      val load: Task[Config]
    }
    trait Live extends Configuration.Service {
      import pureconfig.generic.auto._ //scalafix: ok

      val load: Task[Config] = Task.effect(loadOrThrow[Config])
    }

    val live: Layer[Nothing, Configuration] = ZLayer.succeed(new Live{})
}

