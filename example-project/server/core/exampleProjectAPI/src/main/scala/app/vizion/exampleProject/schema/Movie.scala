package app.vizion.exampleProject.schema

import java.util.UUID
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import io.estatico.newtype.macros.newtype
import doobie.refined.implicits._
import doobie.Read
import doobie.Put
import doobie.postgres.implicits._
import doobie.implicits._
import eu.timepit.refined.api.Refined
import cats._ 
import cats.implicits._
import cats.data.OptionT

object movies{

    @newtype case class MovieId(value: UUID)
    @newtype case class MovieName(value: String)
    @newtype case class MovieYear(value: String)
    @newtype case class MovieDescription(value: String)

    sealed trait Genre
    case object Horror extends Genre
    case object Comedy extends Genre 
    case object Thriller extends Genre
    case object SciFi extends Genre 
    case object Adventure extends Genre
    case object Romance extends Genre

    case class Movie(
        id: MovieId,
        name: MovieName,
        year: MovieYear,
        description: MovieDescription,
        genre: Genre
    )

    def stringToGenre(s: String): Genre = s match{
        case "Horror" => Horror
        case "Comedy" => Comedy
        case "Thriller" => Thriller
        case "SciFi" => SciFi
        case "Adventure" => Adventure
        case "Romance" => Romance
    }

    def genreToString(s: Genre): String = s match{
        case Horror => "Horror"
        case Comedy => "Comedy"
        case Thriller => "Thriller"
        case SciFi => "SciFi"
        case Adventure => "Adventure"
        case Romance => "Romance"
    } 

    implicit val genreMeta : Meta[Genre] = Meta[String].timap(stringToGenre)(genreToString)
}