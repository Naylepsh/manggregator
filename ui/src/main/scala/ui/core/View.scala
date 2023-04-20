package ui.core

import tui.Frame
import tui.crossterm.KeyCode

trait View:
  def render(frame: Frame): Unit
  def handleInput(key: KeyCode): ViewResult

sealed trait ViewResult
object Keep                     extends ViewResult
case class ChangeTo(view: View) extends ViewResult
object Exit                     extends ViewResult
