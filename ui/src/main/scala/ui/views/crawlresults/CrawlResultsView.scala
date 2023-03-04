package ui.views.crawlresults

import ui.core.View
import tui.crossterm.KeyCode
import ui.core.ViewResult
import tui._
import tui.widgets.{BlockWidget, ListWidget}
import library.domain.asset.AssetSummary
import ui.core.StatefulList
import ui.core.Exit
import ui.core.Keep
import cats.implicits._
import ui.core.ChangeTo
import ui.core.Theme
import ui.core.Context
import cats.effect.IO

class CrawlResultsView(
    context: Context[IO],
    crawlResults: List[AssetSummary]
) extends View:

  private val items = StatefulList(items = crawlResults.toArray)

  override def render(frame: Frame): Unit =
    val layout = Layout(
      direction = Direction.Horizontal,
      constraints = Array(Constraint.Percentage(100))
    )

    val items0 = items.items
      .map { case (summary) =>
        val header = Spans.styled(
          summary.asset.name.value,
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
          title =
            Some(Spans.nostyle("Select an asset to see recent releases of:"))
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
        .flatMap(crawlResults.get)
        .map { assetSummary =>
          ChangeTo(ChaptersView(context, assetSummary.asset, assetSummary.chapters))
        }
        .getOrElse(Keep)

    case _ => Keep
