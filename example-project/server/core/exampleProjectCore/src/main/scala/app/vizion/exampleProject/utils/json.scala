package app.vizion.exampleProject

import java.util.UUID

import app.vizion.exampleProject.requests.{NewMovieRequest, NewReviewRequest}
import app.vizion.exampleProject.schema.movies.Movie
import app.vizion.exampleProject.schema.reviews.Review
import cats.effect.Sync
import cats.Monad
import cats.implicits._
import dev.profunktor.auth.jwt.JwtToken
import io.circe._
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto._
import io.circe.refined._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import eu.timepit.refined._
import eu.timepit.refined.api.{Refined, Validate}
import org.http4s._
import app.vizion.exampleProject.schema.movies._
import org.http4s.dsl.Http4sDsl

object json {

  implicit def jsonDecoder[F[_]: Sync, A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def jsonEncoder[F[_]: Sync, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  implicit def coercibleQueryParamDecoder[A: Coercible[B, *], B: QueryParamDecoder]: QueryParamDecoder[A] =
    QueryParamDecoder[B].map(_.coerce[A])

  implicit def refinedQueryParamDecoder[T: QueryParamDecoder, P](
      implicit ev: Validate[T, P]
  ): QueryParamDecoder[T Refined P] =
    QueryParamDecoder[T].emap(refineV[P](_).leftMap(m => ParseFailure(m, m)))

  // ----- Coercible codecs -----
  implicit def coercibleDecoder[A: Coercible[B, *], B: Decoder]: Decoder[A] =
    Decoder[B].map(_.coerce[A])

  implicit def coercibleEncoder[A: Coercible[B, *], B: Encoder]: Encoder[A] =
    Encoder[B].contramap(_.repr.asInstanceOf[B])

  implicit def coercibleKeyDecoder[A: Coercible[B, *], B: KeyDecoder]: KeyDecoder[A] =
    KeyDecoder[B].map(_.coerce[A])

  implicit def coercibleKeyEncoder[A: Coercible[B, *], B: KeyEncoder]: KeyEncoder[A] =
    KeyEncoder[B].contramap[A](_.repr.asInstanceOf[B])

  implicit val newReviewDecoder: Decoder[NewReviewRequest] = deriveDecoder[NewReviewRequest]
  implicit val newReviewEncoder: Encoder[NewReviewRequest] = deriveEncoder[NewReviewRequest]

  implicit val reviewDecoder: Decoder[Review] = deriveDecoder[Review]
  implicit val reviewEncoder: Encoder[Review] = deriveEncoder[Review]

  implicit val newMovieDecoder: Decoder[NewMovieRequest] = deriveDecoder[NewMovieRequest]
  implicit val newMovieEncoder: Encoder[NewMovieRequest] = deriveEncoder[NewMovieRequest]

//movie encode decode

  implicit val encodeMovie: Encoder[Movie] = new Encoder[Movie] {
    final def apply(a: Movie): Json = Json.obj(
      ("id", Json.fromString(a.id.value.toString)),
      ("name", Json.fromString(a.name.value)),
      ("year", Json.fromString(a.year.value)),
      ("description", Json.fromString(a.description.value)),
      ("genre", Json.fromString(genreToString(a.genre)))
    )
  }

  implicit val decodeMovie: Decoder[Movie] = new Decoder[Movie] {
    final def apply(c: HCursor): Decoder.Result[Movie] =
      for {
        id <- c.downField("id").as[String]
        name <- c.downField("name").as[String]
        year <- c.downField("year").as[String]
        description <- c.downField("description").as[String]
        genre <- c.downField("genre").as[String]
      } yield {
         Movie(
          MovieId(UUID.fromString(id)),
          MovieName(name),
          MovieYear(year),
          MovieDescription(description),
          stringToGenre(genre)
        )
      }
  }

}
