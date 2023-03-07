package ui.core

import tui._

case class PaginatedList[A](
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
    val paginated = allItems.grouped(maxItemsOnScreen).toList
    val currentPage = currentIndex.getOrElse(0) / maxItemsOnScreen
    val indexAfterPagination = currentIndex.map(_ % maxItemsOnScreen)

    Pagination(
      paginated(currentPage),
      currentPage,
      indexAfterPagination,
      pages
    )

object PaginatedList:
  case class Pagination[A](
      items: Array[A],
      currentPage: Int,
      currentIndex: Option[Int],
      allPages: Int
  )
