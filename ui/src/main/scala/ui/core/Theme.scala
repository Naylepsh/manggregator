package ui.core

import tui.Color

case class Theme(
    primaryColor: Color
)
object Theme:
  val default = Theme(
    primaryColor = Color.Yellow
  )
