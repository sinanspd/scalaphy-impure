package app.vizion.exampleproject.graphqlserver.services

import com.typesafe.config.ConfigFactory
import zio.stream.ZStream
import zio._
import app.vizion.exampleProject.utils.db._
import app.vizion.exampleProject.algebras.LiveMovieService
import app.vizion.exampleproject.graphqlserver.MainZ.Auth
import doobie.util.transactor.Transactor
import zio.interop.catz._

case class MovieM(
                   name: String,
                   year: String,
                   description: String,
                   genre: String
                 )

object BaseGraphQLService {

  import app.vizion.exampleProject.schema.movies._

  type ExampleService = Has[Service]

  trait Service {
    def getMovies(): RIO[Any, List[MovieM]]

    def getMovieById(id: String): UIO[Option[MovieM]]

    def createMovie(name: String,
                    year: String,
                    description: String,
                    genre: String): RIO[Any, Option[String]]

    def createdMovies: ZStream[Any, Nothing, String]
  }

  def getMovies(): RIO[ExampleService, List[MovieM]] =
    RIO.accessM(_.get.getMovies())

  def getMovieById(id: String): URIO[ExampleService, Option[MovieM]] =
    URIO.accessM(_.get.getMovieById(id))

  def createMovie(name: String,
                  year: String,
                  description: String,
                  genre: String): RIO[ExampleService, Option[String]] =
    URIO.accessM(_.get.createMovie(name, year, description, genre))

  def createdMovies: ZStream[ExampleService, Nothing, String] =
    ZStream.accessStream(_.get.createdMovies)

  val config = ConfigFactory.load()
  val minervaConfig = config.getConfig("app.vizion.exampleproject.api.db")
  val minervaTransactor = transactor(minervaConfig)

  def make(initial: List[Movie], transactor: Transactor[Task]): ZLayer[Any, Nothing, ExampleService] = ZLayer.fromEffect {

    for {
      bands  <- Ref.make(initial)
      subscribers <- Ref.make(List.empty[Queue[String]])
    } yield new Service {

      def getMovies(): Task[List[MovieM]] = new LiveMovieService[Task](transactor).getMovies().foldM(err => ZIO.fail(err), b =>
        ZIO.succeed(b.map(c =>
          MovieM(c.name.value, c.year.value, c.description.value, genreToString(c.genre)))))

      def getMovieById(name: String): UIO[Option[MovieM]] = bands.get.map(_.map(c =>
        MovieM(c.name.value, c.year.value, c.description.value, genreToString(c.genre))).find(c => c.name == name))

      def createMovie(name: String,
                      year: String,
                      description: String,
                      genre: String): Task[Option[String]] =
        new LiveMovieService[Task](transactor).createMovie(MovieName(name), MovieYear(year), MovieDescription(description), stringToGenre(genre))
          .foldM(
            err => ZIO.fail(err),
            _ => ZIO.succeed(Some(name))
          ).tap(created =>
          RIO.when(created.isDefined)(
            subscribers.get.flatMap(
              RIO.foreach(_)(queue =>
                queue.offer(created.get)
                  .onInterrupt(
                    subscribers.update(_.filterNot(_ == queue))
                  )
              )
            )
          )
        )

      def createdMovies: ZStream[Any, Nothing, String] = ZStream.unwrap {
        for {
          queue <- Queue.unbounded[String]
          _ <- subscribers.update(queue :: _)
        } yield ZStream.fromQueue(queue)
      }
    }
  }
}

//object BaseGraphQLService {
//
//  import app.vizion.exampleProject.schema.movies._
//
//  type ExampleService = Has[Service]
//
//  trait Service {
//    def getMovies(): RIO[Any, List[MovieM]]
//
//    def getMovieById(id: String): UIO[Option[MovieM]]
//
//    def createMovie(name: String,
//                    year: String,
//                    description: String,
//                    genre: String): RIO[Any, Option[String]]
//
//    def createdMovies: ZStream[Any, Nothing, String]
//  }
//
//  def getMovies(): RIO[ExampleService, List[MovieM]] =
//    RIO.accessM(_.get.getMovies())
//
//  def getMovieById(id: String): URIO[ExampleService, Option[MovieM]] =
//    URIO.accessM(_.get.getMovieById(id))
//
//  def createMovie(name: String,
//                  year: String,
//                  description: String,
//                  genre: String): RIO[ExampleService, Option[String]] =
//    URIO.accessM(_.get.createMovie(name, year, description, genre))
//
//  def createdMovies: ZStream[ExampleService, Nothing, String] =
//    ZStream.accessStream(_.get.createdMovies)
//
//  val config = ConfigFactory.load()
//  val minervaConfig = config.getConfig("app.vizion.exampleproject.api.db")
//  val minervaTransactor = transactor(minervaConfig)
//
//  def make(initial: List[Movie], transactor: Transactor[Task]): ZLayer[Any, Nothing, ExampleService] = ZLayer.fromEffect {
//
//    for {
//      bands  <- Ref.make(initial)
//      subscribers <- Ref.make(List.empty[Queue[String]])
//    } yield new Service {
//
//      def getMovies(): Task[List[MovieM]] = new LiveMovieService[Task](transactor).getMovies().foldM(err => ZIO.fail(err), b =>
//        ZIO.succeed(b.map(c =>
//          MovieM(c.name.value, c.year.value, c.description.value, genreToString(c.genre)))))
//
//      def getMovieById(name: String): UIO[Option[MovieM]] = bands.get.map(_.map(c =>
//        MovieM(c.name.value, c.year.value, c.description.value, genreToString(c.genre))).find(c => c.name == name))
//
//      def createMovie(name: String,
//                      year: String,
//                      description: String,
//                      genre: String): Task[Option[String]] =
//        new LiveMovieService[Task](transactor).createMovie(MovieName(name), MovieYear(year), MovieDescription(description), stringToGenre(genre))
//          .foldM(
//            err => ZIO.fail(err),
//            _ => ZIO.succeed(Some(name))
//          ).tap(created =>
//          RIO.when(created.isDefined)(
//            subscribers.get.flatMap(
//              RIO.foreach(_)(queue =>
//                queue.offer(created.get)
//                  .onInterrupt(
//                    subscribers.update(_.filterNot(_ == queue))
//                  )
//              )
//            )
//          )
//        )
//
//      def createdMovies: ZStream[Any, Nothing, String] = ZStream.unwrap {
//        for {
//          queue <- Queue.unbounded[String]
//          _ <- subscribers.update(queue :: _)
//        } yield ZStream.fromQueue(queue)
//      }
//    }
//  }
//}