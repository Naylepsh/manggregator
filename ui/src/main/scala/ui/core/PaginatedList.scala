package ui.core

import tui._

case class PaginatedList[A](
    allItems: Array[A]
):
  import PaginatedList._

  def paginate(area: Rect, itemHeight: Int, currentIndex: Int): Pagination[A] =
    val maxItemsOnScreen = (area.height / itemHeight).max(2)
    val pages = allItems.length / maxItemsOnScreen
    val currentPage = currentIndex / maxItemsOnScreen
    val indexAfterPagination = currentIndex % maxItemsOnScreen
    val paginated = allItems.grouped(maxItemsOnScreen).toList

    Pagination(
      paginated(currentPage),
      currentPage,
      indexAfterPagination,
      pages - 1
    )

object PaginatedList:
  case class Pagination[A](
      items: Array[A],
      currentPage: Int,
      currentIndex: Int,
      allPages: Int
  )
