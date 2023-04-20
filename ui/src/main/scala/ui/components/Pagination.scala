package ui.components

import tui.*
import tui.widgets.BlockWidget

object Pagination:
  def render(
      frame: Frame,
      area: Rect,
      currentPage: Int,
      allPages: Int
  ): Unit =
    val block = BlockWidget(
      borders = Borders.NONE,
      title = Some(
        Spans.nostyle(s"${currentPage + 1} / ${allPages}")
      )
    )

    frame.render_widget(block, area)
