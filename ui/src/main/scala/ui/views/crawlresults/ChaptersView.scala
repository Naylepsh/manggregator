package ui.views.crawlresults

import ui.core.View
import tui.crossterm.KeyCode
import ui.core.ViewResult
import tui._
import tui.widgets.{BlockWidget, ListWidget}
import cats.implicits._
import ui.core.Exit
import ui.core.Keep
import library.domain.chapter.Chapter
import ui.core.StatefulList
import ui.core.Theme
import library.domain.asset.Asset
import ui.core.Context
import cats.effect.IO

class ChaptersView(
    context: Context[IO],
    asset: Asset,
    chapters: List[Chapter]
) extends View:

  private val items = StatefulList(items = chapters.toArray)

  override def render(frame: Frame): Unit =
    val layout = Layout(
      direction = Direction.Horizontal,
      constraints = Array(Constraint.Percentage(100))
    )

    val items0 = items.items
      .map { case (chapter) =>
        val lines = Array(
          Spans.styled(
            chapter.no.value,
            Style(fg = Some(Color.Gray))
          ),
          Spans.nostyle(chapter.url.value)
        )

        ListWidget.Item(Text(lines))
      }

    val widget = ListWidget(
      items = items0,
      block = Some(
        BlockWidget(
          borders = Borders.ALL,
          title = Some(Spans.nostyle(s"${asset.name.value} releases"))
        )
      ),
      highlight_style =
        Style(bg = Some(context.theme.primaryColor), add_modifier = Modifier.BOLD),
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
        .flatMap(chapters.get)
        .map { chapter =>
          Exit
        }
        .getOrElse(Keep)

    case _ => Keep
