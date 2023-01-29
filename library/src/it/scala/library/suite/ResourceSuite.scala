package library.suite

import cats.effect._
import cats.syntax.flatMap._
import weaver.{Expectations, IOSuite}

abstract class ResourceSuite extends IOSuite {

  /** Shamelessly copied from "Practical FP in Scala" book:
    * https://github.com/gvolpe/pfps-shopping-cart/blob/second-edition/modules/tests/src/main/scala/suite/ResourceSuite.scala
    */

  implicit class SharedResOps(res: Resource[IO, Res]) {
    def beforeAll(f: Res => IO[Unit]): Resource[IO, Res] =
      res.evalTap(f)

    def afterAll(f: Res => IO[Unit]): Resource[IO, Res] =
      res.flatTap(x => Resource.make(IO.unit)(_ => f(x)))
  }

  def testBeforeAfterEach(
      before: Res => IO[Unit],
      after: Res => IO[Unit]
  ): String => (Res => IO[Expectations]) => Unit =
    name =>
      fa => test(name)(res => before(res) >> fa(res).guarantee(after(res)))

  def testAfterEach(
      after: Res => IO[Unit]
  ): String => (Res => IO[Expectations]) => Unit =
    testBeforeAfterEach(_ => IO.unit, after)

  def testBeforeEach(
      before: Res => IO[Unit]
  ): String => (Res => IO[Expectations]) => Unit =
    testBeforeAfterEach(before, _ => IO.unit)

}
