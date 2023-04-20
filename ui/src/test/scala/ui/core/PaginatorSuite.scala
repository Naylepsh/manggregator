package ui.core

import cats.implicits.*
import cats.kernel.Monoid
import ui.suite.ParametrizedSuite
import weaver.{Expectations, SimpleIOSuite}

object PaginatorSuite extends ParametrizedSuite:

  parametrizedTest("Test page count")(
    List(
      (2, 2),
      (1, 4),
      (3, 2),
      (4, 1),
      (5, 1)
    )
  ) {
    case (maxItemsOnScreen, expectedPageCount) =>
      val items = Array(1, 2, 3, 4)
      val pagination =
        Paginator(items).paginate(maxItemsOnScreen, currentIndex = None)

      expect(
        pagination.pageCount == expectedPageCount,
        s"""Unexpected page count for $maxItemsOnScreen max items on screen.
      Expected $expectedPageCount, got ${pagination.pageCount}""".stripMargin
      )
  }

  pureTest("Going to next page when at last page loops back to start") {

    /**
     * Pages:
     *   - 1: (1, 2)
     *   - 2: (3, 4)
     *   - 3: (5)
     */

    val items = Array(1, 2, 3, 4, 5)
    val pagination =
      Paginator(items).paginate(maxItemsOnScreen = 2, currentIndex = Some(4))

    expect.all(
      pagination.currentPage == 2,
      pagination.nextPage().currentPage == 0
    )
  }

  pureTest("Going to previous page when at first page loops back to end") {

    /**
     * Pages:
     *   - 1: (1, 2)
     *   - 2: (3, 4)
     *   - 3: (5)
     */

    val items = Array(1, 2, 3, 4, 5)
    val pagination =
      Paginator(items).paginate(maxItemsOnScreen = 2, currentIndex = Some(0))

    expect.all(
      pagination.currentPage == 0,
      pagination.previousPage().currentPage == 2
    )
  }
