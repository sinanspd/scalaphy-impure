package app.vizion.exampleProject.algebras

import cats._
import cats.implicits._
import cats.effect.Sync
import doobie.util.transactor.Transactor
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import app.vizion.exampleProject.utils.db._
import java.util.UUID
import app.vizion.exampleProject.effects.GenUUID
import app.vizion.exampleProject.effects.effects.BracketThrow
import app.vizion.exampleProject.schema.movies.{
  Genre,
  MovieDescription,
  MovieId,
  MovieName,
  MovieYear,
  Movie => TMovie
}

trait Movies[F[_]] {
  def getMovies(): F[List[TMovie]]
  def getMovieById(id: UUID): F[Option[TMovie]]
  def createMovie(
      name: MovieName,
      year: MovieYear,
      description: MovieDescription,
      genre: Genre
  ): F[String]
  def createMoviesBatch(s: List[TMovie]): fs2.Stream[F, TMovie]
  def deleteMovieById(id: UUID): F[Int]
  def updateMovie(name: TMovie): F[TMovie]
}

object LiveMovies {
  def make[F[_]: Sync](
      xa: Transactor[F]
  ): F[Movies[F]] =
    Sync[F].delay(
      new LiveMovieService(xa)
    )
}

class LiveMovieService[F[_]: GenUUID: BracketThrow](xa: Transactor[F]) extends Movies[F] {
  def getMovies(): F[List[TMovie]] = {
    val query = sql"SELECT * FROM movies".query[TMovie].to[List]
    query.transact(xa)
  }

  def getMovieById(id: UUID): F[Option[TMovie]] = {
    val query = sql"SELECT * FROM movies WHERE id = (${id})::uuid".query[TMovie].to[List]
    query.transact(xa).map(_.headOption)
  }
  def createMovie(
      name: MovieName,
      year: MovieYear,
      description: MovieDescription,
      genre: Genre
  ): F[String] =
    GenUUID[F].make[MovieId].flatMap { id =>
      val query: ConnectionIO[String] =
        sql"INSERT INTO movies (id, name, year, description, genre) VALUES ((${id.toString})::uuid, ${name}, ${year}, ${description}, ${genre})".update
          .withUniqueGeneratedKeys("id")
      query.transact(xa)
    }

  def deleteMovieById(id: UUID): F[Int] = {
    val query = sql"DELETE FROM movies WHERE id = (${id})::uuid ".update.run
    query.transact(xa)
  }

  def updateMovie(movieUp: TMovie): F[TMovie] = {
    val query =
      sql"UPDATE movies SET name = ${movieUp.name}, year = ${movieUp.year}, description = ${movieUp.description}, genre = ${movieUp.genre}  WHERE id = (${movieUp.id.value})::uuid".update.run
    query.transact(xa).map(a => movieUp)
  }
  def createMoviesBatch(s: List[TMovie]): fs2.Stream[F, TMovie] = ???
}
