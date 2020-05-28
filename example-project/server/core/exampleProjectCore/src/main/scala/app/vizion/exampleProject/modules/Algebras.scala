package app.vizion.exampleProject.modules

import cats.Parallel
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.algebra.RedisCommands
import doobie.util.transactor.Transactor

object Algebras{
    def make[F[_]: Concurrent: Parallel: Timer](
        xa: Transactor[F]
    ): F[Algebras[F]] = 
        for{
            movies <- LiveMovies.make[F](xa)
            reviews <- LiveReviews.make[F](xa)
        }yield new Algebras[F](moviews, reviews)
}

final class Algebras[F[_]] private(
    val movies: Movies[F],
    val reviews: Reviews[F],
){}