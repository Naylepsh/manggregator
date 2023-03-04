package ui.core

import cats.effect.Sync
import cats.implicits._
import tui.widgets.ListWidget

case class StatefulList[T](
    state: ListWidget.State = ListWidget.State(),
    items: Array[T]
) {
  def next(): Unit = {
    val i = state.selected match {
      case Some(i) => if (i >= items.length - 1) 0 else i + 1
      case None    => 0
    }
    state.select(Some(i))
  }

  def previous(): Unit = {
    val i = state.selected match {
      case Some(i) =>
        if (i == 0) {
          items.length - 1
        } else {
          i - 1
        }
      case None => 0
    }
    state.select(Some(i))
  }

  def unselect(): Unit =
    state.select(None)
}
