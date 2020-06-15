package app.vizion.graphqlserver.configuration

import app.vizion.exampleProject.auth.schema.auth.CommonUser
import caliban.{GraphQLInterpreter, GraphQLRequest, GraphQLResponse}
import caliban.ResponseValue.{ObjectValue, StreamValue}
import caliban.Value.NullValue
import cats.arrow.FunctionK
import cats.data.{Kleisli, OptionT}
import cats.effect.Effect
import cats.effect.syntax.all._
import cats.~>
//import com.github.ghik.silencer.silent
import fs2.{Pipe, Stream}
import io.circe.Decoder.Result
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text
import zio._
import zio.clock.Clock
import zio.duration.Duration
import zio.interop.catz._

object calibanExtension {

  private def executeToJson[R, E](
                                   interpreter: GraphQLInterpreter[R, E],
                                   request: GraphQLRequest,
                                   skipValidation: Boolean,
                                   enableIntrospection: Boolean
                                 ): URIO[R, Json] =
    interpreter
      .executeRequest(request, skipValidation = skipValidation, enableIntrospection = enableIntrospection)
      .foldCause(cause => GraphQLResponse(NullValue, cause.defects).asJson, _.asJson)

  private def getGraphQLRequest(
                                 query: String,
                                 op: Option[String],
                                 vars: Option[String],
                                 exts: Option[String]
                               ): Result[GraphQLRequest] = {
    val variablesJs  = vars.flatMap(parse(_).toOption)
    val extensionsJs = exts.flatMap(parse(_).toOption)
    val fields = List("query" -> Json.fromString(query)) ++
      op.map(o => "operationName"         -> Json.fromString(o)) ++
      variablesJs.map(js => "variables"   -> js) ++
      extensionsJs.map(js => "extensions" -> js)
    Json
      .fromFields(fields)
      .as[GraphQLRequest]
  }

  private def getGraphQLRequest(params: Map[String, String]): Result[GraphQLRequest] =
    getGraphQLRequest(
      params.getOrElse("query", ""),
      params.get("operationName"),
      params.get("variables"),
      params.get("extensions")
    )

  def makeAuthedHttpService[R, E](
                                              interpreter: GraphQLInterpreter[R, E],
                                              skipValidation: Boolean = false,
                                              enableIntrospection: Boolean = true
                                            ): AuthedRoutes[CommonUser, RIO[R, *]] = {
    object dsl extends Http4sDsl[RIO[R, *]]
    import dsl._

    AuthedRoutes.of[CommonUser, RIO[R, *]] {
      case req @ POST -> Root as _ =>
        for {
          query <- req.req.attemptAs[GraphQLRequest].value.absolve
          result <- executeToJson(
            interpreter,
            query,
            skipValidation = skipValidation,
            enableIntrospection = enableIntrospection
          )
          response <- Ok(result)
        } yield response
      case req @ GET -> Root as _=>
        for {
          query <- Task.fromEither(getGraphQLRequest(req.req.params))
          result <- executeToJson(
            interpreter,
            query,
            skipValidation = skipValidation,
            enableIntrospection = enableIntrospection
          )
          response <- Ok(result)
        } yield response
    }
  }

}
