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

object reviews {

  implicit def newTypePut[N: Coercible[R, *], R: Put]: Put[N] = Put[R].contramap[N](_.repr.asInstanceOf[R])

  implicit def newTypeRead[N: Coercible[R, *], R: Read]: Read[N] = Read[R].map(_.asInstanceOf[N])

  /** If we have an Eq instance for Repr type R, derive an Eq instance for  NewType N. */
  implicit def coercibleEq[R, N](implicit ev: Coercible[Eq[R], Eq[N]], R: Eq[R]): Eq[N] =
    ev(R)

  @newtype case class ReviewId(value: UUID)
  @newtype case class UserId(value: UUID)
  @newtype case class ReviewBody(value: String)
  @newtype case class ReviewDate(value: Long)

  case class Review(
      id: ReviewId,
      user: UserId,
      body: ReviewBody,
      date: ReviewDate
  )
}
