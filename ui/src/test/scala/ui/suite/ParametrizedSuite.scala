package ui.suite

import cats.implicits.*
import cats.kernel.Monoid
import weaver.{Expectations, SimpleIOSuite}

abstract class ParametrizedSuite extends SimpleIOSuite:
  def parametrizedTest[A](
      name: String
  )(params: List[A])(run: A => Expectations): Unit =
    pureTest(name)(params.foldLeft(Monoid[Expectations].empty) {
      case (expectations, a) => expectations and run(a)
    })
