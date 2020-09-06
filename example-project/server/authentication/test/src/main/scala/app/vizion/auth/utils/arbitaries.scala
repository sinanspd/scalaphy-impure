package app.vizion.utils

import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import java.util.UUID
import org.scalacheck.{ Arbitrary, Gen }
import app.vizion.utils.generators._

object arbitraries {

  implicit def arbCoercibleInt[A: Coercible[Int, *]]: Arbitrary[A] =
    Arbitrary(Gen.posNum[Int].map(_.coerce[A]))

  implicit def arbCoercibleStr[A: Coercible[String, *]]: Arbitrary[A] =
    Arbitrary(cbStr[A])

  implicit def arbCoercibleUUID[A: Coercible[UUID, *]]: Arbitrary[A] =
    Arbitrary(cbUuid[A])
}
