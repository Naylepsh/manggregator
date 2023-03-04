package ui.views

import ui.core.View
import tui.crossterm.KeyCode
import tui.widgets.{BlockWidget, ListWidget}
import ui.core.ViewResult
import tui._
import ui.core.Exit
import ui.core.Keep
import ui.core.StatefulList
import cats.implicits._
import ui.core.Theme
import ui.core.ChangeTo

class MainMenuView(theme: Theme) extends View:

  case class Action(label: String, onSelect: () => View)

  private val actions = List(
    Action("Trigger crawl", () => this),
    Action("Browse recent releases", () => this),
    Action("Manage assets", () => this)
  )
  private val items = StatefulList(items = actions.toArray)

  override def render(frame: Frame): Unit =
    val layout = Layout(
      direction = Direction.Horizontal,
      constraints = Array(Constraint.Percentage(100))
    )

    val items0 = items.items
      .map { case (action) =>
        val header = Spans.styled(
          action.label,
          Style(fg = Some(Color.Gray))
        )

        val lines = Array(header)

        ListWidget.Item(Text(lines))
      }

    val widget = ListWidget(
      items = items0,
      block = Some(
        BlockWidget(
          borders = Borders.ALL,
          title = Some(Spans.nostyle("Main menu"))
        )
      ),
      highlight_style =
        Style(bg = Some(theme.primaryColor), add_modifier = Modifier.BOLD),
      highlight_symbol = Some(">> ")
    )

    frame
      .render_stateful_widget(widget, layout.split(frame.size).head)(
        items.state
      )

  override def handleInput(key: KeyCode): ViewResult = key match
    case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => Exit
    case _: tui.crossterm.KeyCode.Down => items.next(); Keep
    case _: tui.crossterm.KeyCode.Up   => items.previous(); Keep
    case _: tui.crossterm.KeyCode.Enter =>
      items.state.selected
        .flatMap(actions.get)
        .map { action =>
          ChangeTo(action.onSelect())
        }
        .getOrElse(Keep)

    case _ => Keep
