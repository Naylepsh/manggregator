package manggregator.apps

import cats.effect.IO
import cats.effect.ExitCode
import ui.views.MainMenuView
import ui.core.RenderLoop
import cats.effect.unsafe.implicits.global
import ui.core.Theme

object UI:
  def run(): IO[ExitCode] =
    val theme = Theme.default
    val view = MainMenuView(theme)
    RenderLoop(view).run().as(ExitCode.Success)
