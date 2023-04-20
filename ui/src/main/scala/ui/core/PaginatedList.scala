package ui.core

import scala.reflect.ClassTag

import library.domain.chapter.Chapter
import tui.*
import ui.core.Paginator.Pagination

case class PaginatedList[A: ClassTag](
    allItems: Array[A]
):
  private val items                                       = StatefulList(items = allItems)
  private var paginatedList                               = Paginator(allItems)
  private var pagination: Option[Paginator.Pagination[A]] = None

  def selectedIndex: Option[Int] = items.state.selected
  def selected: Option[A]        = items.selected

  def update(index: Int, value: A): Option[Unit] =
    items.update(index, value).map { _ => this.paginatedList = Paginator(items.items) }

  def paginate(area: Rect, itemHeight: Int): Pagination[A] =
    val pagination = paginatedList.paginate(
      area,
      itemHeight,
      items.state.selected
    )
    this.pagination = Some(pagination)
    pagination

  def nextItem(): Unit =
    items.next()

  def nextPage(): Unit =
    pagination.foreach { p =>
      val newPagination = p.nextPage()
      newPagination.absoluteIndex().foreach(items.to)
      this.pagination = Some(newPagination)
    }

  def previousItem(): Unit =
    items.previous()

  def previousPage(): Unit =
    pagination.foreach { p =>
      val newPagination = p.previousPage()
      newPagination.absoluteIndex().foreach(items.to)
      this.pagination = Some(newPagination)
    }
