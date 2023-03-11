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
import cats.effect.unsafe.IORuntime
import ui.components.KeybindsNav
import ui.core.PaginatedList
import tui.widgets.ListWidget.State
import ui.components.Pagination

class CrawlResultsView(
    context: Context[IO],
    crawlResults: List[AssetSummary]
)(using IORuntime)
    extends View:

  private val results = crawlResults.sortBy(_.asset.name)
  private val paginatedList = PaginatedList(results.toArray)
  private val keyBindsNav = KeybindsNav(
    List("↑ up", "↓ down", "s mark as seen", "q quit")
  )

  private val crawlResultHeight = 2

  private def renderAssets(
      frame: Frame,
      area: Rect,
      crawlResultsPage: Array[AssetSummary],
      selected: Option[Int]
  ): Unit =
    val padding = " " * 3
    val items0 = crawlResultsPage
      .map { case (summary) =>
        val lines = Array(
          Spans.nostyle(""),
          Spans.styled(
            s"$padding${summary.asset.name.value}",
            Style(fg = Some(Color.White))
          )
        )

        ListWidget.Item(Text(lines))
      }

    val widget = ListWidget(
      items = items0,
      block = Some(
        BlockWidget(
          borders = Borders.NONE,
          title = Some(
            Spans.from(
              Span.nostyle("Select an asset to see recent releases of"),
              Span.nostyle(s" - ${results.length} assets")
            )
          )
        )
      ),
      highlight_style = Style(
        fg = Some(context.theme.primaryColor),
        add_modifier = Modifier.BOLD
      )
    )

    frame.render_stateful_widget(widget, area)(State(selected = selected))

  override def render(frame: Frame): Unit =
    val chunks = Layout(
      direction = Direction.Vertical,
      constraints = Array(
        Constraint.Percentage(80),
        Constraint.Percentage(10),
        Constraint.Percentage(10)
      )
    ).split(frame.size)

    chunks.toList match
      case main :: paginationArea :: nav :: Nil =>
        val pagination = paginatedList.paginate(
          main,
          crawlResultHeight
        )

        renderAssets(
          frame,
          main,
          pagination.pages(pagination.currentPage),
          pagination.currentIndex
        )
        Pagination.render(
          frame,
          paginationArea,
          pagination.currentPage,
          pagination.pageCount
        )
        keyBindsNav.render(frame, nav)
      case _ =>

  override def handleInput(key: KeyCode): ViewResult = key match
    case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => Exit
    case _: tui.crossterm.KeyCode.Down  => paginatedList.nextItem(); Keep
    case _: tui.crossterm.KeyCode.Up    => paginatedList.previousItem(); Keep
    case _: tui.crossterm.KeyCode.Right => paginatedList.nextPage(); Keep
    case _: tui.crossterm.KeyCode.Left  => paginatedList.previousPage(); Keep
    case _: tui.crossterm.KeyCode.Enter =>
      paginatedList.selected
        .flatMap(results.get)
        .map { assetSummary =>
          ChangeTo(
            ChaptersView(context, assetSummary.asset, assetSummary.chapters)
          )
        }
        .getOrElse(Keep)

    case _ => Keep
