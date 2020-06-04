package app.vizion.exampleproject.graphqlserver

import app.vizion.exampleproject.graphqlserver.services.BaseGraphQLService.ExampleService
import app.vizion.exampleproject.graphqlserver.services.{BaseGraphQLService, MovieM}
import app.vizion.exampleProject.schema.movies._
import caliban.GraphQL
import caliban.schema.Annotations.{GQLDeprecated, GQLDescription}
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{maxDepth, maxFields, printSlowQueries, timeout}
import zio.clock.Clock
import zio.console.Console
import zio.duration._
import zio.stream.ZStream
import zio.URIO
import zio.RIO
import app.vizion.exampleProject.algebras.LiveMovies

import scala.language.postfixOps
import caliban.schema.GenericSchema
import caliban.GraphQL.graphQL
import caliban.RootResolver
import doobie.util.transactor.Transactor
import monix.eval.Task
import zio.blocking.Blocking


class BaseGraphQLApi(xa: Transactor[zio.Task]) extends GenericSchema[ExampleService]{
  // type AppEnvironment = Console with Clock with Blocking with Persistence with ExampleService

  case class CharacterArgs(name: String, city: String, country: String, logo: String)
    case class Queries(
                        @GQLDescription("Return all characters from a given origin")
                        bands:  RIO[ExampleService, List[MovieM]],
                        @GQLDeprecated("Use `characters`")
                        band: CharacterArgs => RIO[ExampleService, Option[MovieM]]
                      )
    case class Mutations(addBand: CharacterArgs => RIO[ExampleService, Option[String]])
    case class Subscriptions(characterDeleted: ZStream[ExampleService, Nothing, String])

    //implicit val characterSchema      = gen[Movie]

    implicit val characterSchema      = gen[MovieM]
    implicit val characterArgsSchema  = gen[CharacterArgs]

    val api: GraphQL[Console with Clock with ExampleService] =
      graphQL(
        RootResolver(
          Queries(
            BaseGraphQLService.getMovies(),
            args => BaseGraphQLService.getMovieById(args.name)
          ),
          Mutations(args => BaseGraphQLService.createMovie(args.name, args.city, args.country, args.logo)),
          Subscriptions(BaseGraphQLService.createdMovies)
        )
      ) @@
        maxFields(200) @@               // query analyzer that limit query fields
        maxDepth(30) @@                 // query analyzer that limit query depth
        timeout(3 seconds) @@           // wrapper that fails slow queries
        printSlowQueries(500 millis) @@ // wrapper that logs slow queries
        apolloTracing                   // wrapper for https://github.com/apollographql/apollo-tracing
}