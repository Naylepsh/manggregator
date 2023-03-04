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
import ui.components.KeybindsNav

class ChaptersView(
    context: Context[IO],
    asset: Asset,
    chapters: List[Chapter]
) extends View:

  private val items = StatefulList(items = chapters.toArray)
  private val keyBindsNav = KeybindsNav(
    List("↑ up", "↓ down", "enter select", "q quit")
  )

  override def render(frame: Frame): Unit =
    val chunks = Layout(
      direction = Direction.Vertical,
      constraints = Array(Constraint.Percentage(90), Constraint.Percentage(10))
    ).split(frame.size)

    chunks.toList match
      case main :: bottom :: Nil =>
        renderChapters(frame, main)
        keyBindsNav.render(frame, bottom)
      case _ =>

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

  private def renderChapters(frame: Frame, area: Rect): Unit =
    val padding = " " * 3
    val chapterListItems = items.items
      .map { case (chapter) =>
        val lines = Array(
          Spans.nostyle(""),
          Spans.styled(
            s"$padding${chapter.no.value}",
            Style(fg = Some(Color.White))
          ),
          Spans.nostyle(s"$padding${chapter.url.value}")
        )

        ListWidget.Item(Text(lines))
      }

    val chapterWidget = ListWidget(
      items = chapterListItems,
      block = Some(
        BlockWidget(
          borders = Borders.NONE,
          title = Some(
            Spans.from(
              Span.styled(asset.name.value, Style(fg = Some(Color.White))),
              Span.nostyle(s" - ${items.items.length} chapters")
            )
          )
        )
      ),
      highlight_style = Style(
        fg = Some(context.theme.primaryColor),
        add_modifier = Modifier.BOLD
      )
    )

    frame
      .render_stateful_widget(chapterWidget, area)(items.state)
