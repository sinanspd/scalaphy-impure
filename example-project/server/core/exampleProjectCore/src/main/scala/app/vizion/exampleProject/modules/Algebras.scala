package app.vizion.exampleProject.modules

import app.vizion.exampleProject.algebras.{ LiveMovies, LiveReviews, Movies, Review }
import cats.Parallel
import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor

object Algebras {
  def make[F[_]: Concurrent: Parallel: Timer](
      xa: Transactor[F]
  ): F[Algebras[F]] =
    for {
      movies <- LiveMovies.make[F](xa)
      reviews <- LiveReviews.make[F](xa)
    } yield new Algebras[F](movies, reviews)
}

final class Algebras[F[_]] private (
    val movies: Movies[F],
    val reviews: Review[F]
) {}
