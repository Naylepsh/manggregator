package ui.suite

import weaver.SimpleIOSuite
import weaver.Expectations
import cats.kernel.Monoid
import cats.implicits._

abstract class ParametrizedSuite extends SimpleIOSuite:
  def parametrizedTest[A](
      name: String
  )(params: List[A])(run: A => Expectations): Unit =
    pureTest(name)(params.foldLeft(Monoid[Expectations].empty) {
      case (expectations, a) => expectations and run(a)
    })
