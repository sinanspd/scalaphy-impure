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
import app.vizion.exampleProject.schema.reviews.{ReviewBody, ReviewDate, ReviewId, UserId, Review => TReview}


trait Review[F[_]]{
    def getReviews(): F[List[TReview]]
    def getReviewById(id: UUID): F[Option[TReview]]
    def createReview(
        user: UserId,
        body: ReviewBody,
        date: ReviewDate
    ): F[String]
    def deleteReviewById(id: UUID): F[Int]
   // def createReviewBatch(s: List[TReview]): fs2.Stream[F, TReview]
}

object LiveReviews{
 def make[F[_] : Sync](
        xa: Transactor[F]
    ): F[Review[F]] =
    Sync[F].delay(
        new LiveReviewService(xa)
    )
}

class LiveReviewService[F[_]: GenUUID: BracketThrow](xa: Transactor[F]) extends Review[F]{
    def getReviews(): F[List[TReview]] = {
        val query = sql"SELECT * FROM reviews".query[TReview].to[List]
        query.transact(xa)
    }

    def getReviewById(id: UUID): F[Option[TReview]] = {
        val query = sql"SELECT * FROM reviews WHERE id = (${id})::uuid".query[TReview].to[List]
        query.transact(xa).map(_.headOption)
    }
    def createReview(
        user: UserId,
        body: ReviewBody,
        date: ReviewDate
    ): F[String] = {
        GenUUID[F].make[ReviewId].flatMap{id => 
            val query : ConnectionIO[String]  = sql"INSERT INTO reviews (id, user, body, date) VALUES ((${id.toString})::uuid, ${user}, ${body}, ${date})"
                        .update
                        .withUniqueGeneratedKeys("id")
            query.transact(xa)
        }
    }

    def deleteReviewById(id: UUID): F[Int] = {
        val query = sql"DELETE FROM reviews WHERE id = (${id})::uuid ".update.run
        query.transact(xa)
    }

   // def createReviewsBatch(s: List[TReview]): fs2.Stream[F, TReview] = ???
}