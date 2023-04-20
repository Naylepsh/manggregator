package ui.components

import tui.*
import tui.widgets.BlockWidget

class KeybindsNav(keyBinds: List[String], separator: String):
  def render(frame: Frame, area: Rect): Unit =
    val block = BlockWidget(
      title = Some(
        Spans.styled(
          keyBinds.mkString(separator),
          Style(
            fg = Some(Color.DarkGray)
          )
        )
      ),
      title_alignment = Alignment.Center
    )

    frame.render_widget(block, area)

object KeybindsNav:
  def apply(keyBinds: List[String]): KeybindsNav =
    new KeybindsNav(keyBinds, " :: ")
