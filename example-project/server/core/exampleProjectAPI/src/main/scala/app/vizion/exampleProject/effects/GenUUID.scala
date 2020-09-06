package app.vizion.exampleProject.effects

import cats.effect.Sync
import cats.implicits._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import java.util.UUID
import app.vizion.exampleProject.effects.effects._

trait GenUUID[F[_]] {
  def make: F[UUID]
  def make[A: Coercible[UUID, *]]: F[A]
  def makeM(n: Int): F[List[UUID]]
  def makeMany[A: Coercible[UUID, *]](n: Int): F[List[A]]
  def read[A: Coercible[UUID, *]](str: String): F[A]
}

object GenUUID {
  def apply[F[_]](implicit ev: GenUUID[F]): GenUUID[F] = ev

  implicit def syncGenUUID[F[_]: Sync]: GenUUID[F] =
    new GenUUID[F] {
      def make: F[UUID] =
        Sync[F].delay(UUID.randomUUID())

      def makeM(n: Int): F[List[UUID]] = Sync[F].delay(List.fill(n)(UUID.randomUUID()))

      def make[A: Coercible[UUID, *]]: F[A] =
        make.map(_.coerce[A])

      def makeMany[A: Coercible[UUID, *]](n: Int): F[List[A]] =
        makeM(n).map(_.map(_.coerce[A]))

      def read[A: Coercible[UUID, *]](str: String): F[A] =
        ApThrow[F].catchNonFatal(UUID.fromString(str).coerce[A])
    }
}
