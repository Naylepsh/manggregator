package ui.core

import scala.reflect.ClassTag

import tui._

case class Paginator[A: ClassTag](
    allItems: Array[A]
):
  import Paginator._

  def paginate(
      area: Rect,
      itemHeight: Int,
      currentIndex: Option[Int]
  ): Pagination[A] =
    paginate(
      (area.height / itemHeight - 1).max(1),
      currentIndex
    )

  def paginate(
      maxItemsOnScreen: Int,
      currentIndex: Option[Int]
  ): Pagination[A] =
    val pageCount =
      Math.ceil(allItems.length / maxItemsOnScreen.doubleValue).toInt
    val paginated = allItems.grouped(maxItemsOnScreen).toArray
    val currentPage = currentIndex.getOrElse(0) / maxItemsOnScreen
    val indexAfterPagination = currentIndex.map(_ % maxItemsOnScreen)

    Pagination(
      paginated,
      currentPage,
      indexAfterPagination,
      pageCount
    )

object Paginator:
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
