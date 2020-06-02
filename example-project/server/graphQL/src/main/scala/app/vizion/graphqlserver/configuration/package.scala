package app.vizion.exampleproject.graphqlserver

import zio.{RIO, ZIO}

package object configuration {
    type Configuration = zio.Has[Configuration.Service]
    def loadConfig: RIO[Configuration, Config]= ZIO.accessM(_.get.load)
}
