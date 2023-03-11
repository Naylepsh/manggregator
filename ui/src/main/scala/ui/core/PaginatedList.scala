package ui.core

import tui._
import scala.reflect.ClassTag

case class PaginatedList[A: ClassTag](
    allItems: Array[A]
):
  import PaginatedList._

  def paginate(
      area: Rect,
      itemHeight: Int,
      currentIndex: Option[Int]
  ): Pagination[A] =
    val maxItemsOnScreen = (area.height / itemHeight).max(1)
    val pages = allItems.length / maxItemsOnScreen
    val paginated = allItems.grouped(maxItemsOnScreen).toArray
    val currentPage = currentIndex.getOrElse(0) / maxItemsOnScreen
    val indexAfterPagination = currentIndex.map(_ % maxItemsOnScreen)

    Pagination(
      paginated,
      currentPage,
      indexAfterPagination,
      pages
    )

object PaginatedList:
  case class Pagination[A](
      pages: Array[Array[A]],
      currentPage: Int,
      currentIndex: Option[Int],
      pageCount: Int
  ):
    def nextPage(): Pagination[A] =
      this.copy(
        currentPage = (currentPage + 1) % pages.length,
        currentIndex = currentIndex.map(_ => 0)
      )

    def previousPage(): Pagination[A] =
      this.copy(
        currentPage = (currentPage - 1 + pages.length) % pages.length,
        currentIndex = currentIndex.map(_ => 0)
      )

    def absoluteIndex(): Option[Int] =
      for
        relativeIndex <- currentIndex
        pageSize <- pages.headOption.map(_.length)
      yield currentPage * pageSize + relativeIndex
