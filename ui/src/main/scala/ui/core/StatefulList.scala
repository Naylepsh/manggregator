package ui.core

import cats.effect.Sync
import cats.implicits.*
import tui.widgets.ListWidget

case class StatefulList[T](
    state: ListWidget.State = ListWidget.State(),
    items: Array[T]
):
  def next(): Unit =
    val i = state.selected match
      case Some(i) => if i >= items.length - 1 then 0 else i + 1
      case None    => 0
    state.select(Some(i))

  def previous(): Unit =
    val i = state.selected match
      case Some(i) =>
        if i == 0 then
          items.length - 1
        else
          i - 1
      case None => 0
    state.select(Some(i))

  def to(index: Int): Unit =
    val newIndex = if 0 <= index && index < items.length then index else 0
    state.select(Some(index))

  def unselect(): Unit =
    state.select(None)

  def selected: Option[T] =
    state.selected.map(index => items(index))

  def update(index: Int, value: T): Option[Unit] =
    if index < 0 || index >= items.length then None
    else
      items.update(index, value)
      Some(())
