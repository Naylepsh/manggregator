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
    val maxItemsOnScreen = (area.height / itemHeight).max(2)
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

  // def nextPage(pagination: Pagination[A]): Pagination[A] = ???

object PaginatedList:
  case class Pagination[A](
      pages: Array[Array[A]],
      currentPage: Int,
      currentIndex: Option[Int],
      pageCount: Int
  )
