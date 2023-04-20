package ui.views.assetmanagement.addpage

import cats.effect.IO
import library.domain.page.Site
import tui.*
import tui.crossterm.KeyCode
import tui.widgets.ListWidget.State
import tui.widgets.{ BlockWidget, ListWidget }
import ui.components.{ KeybindsNav, Pagination }
import ui.core.*

class ChooseSiteView(
    context: Context[IO],
    nextView: Site => View,
    previousView: Option[View]
) extends View:
  import ChooseSiteView.*

  private val paginatedList = PaginatedList(sites)

  override def render(frame: Frame): Unit =
    val chunks = Layout(
      direction = Direction.Vertical,
      constraints = Array(
        Constraint.Percentage(80),
        Constraint.Percentage(10),
        Constraint.Percentage(10)
      )
    ).split(frame.size)

    chunks match
      case Array(main, nav, paginationArea) =>
        val pagination = paginatedList.paginate(main, itemHeight = 1)

        renderSiteSelection(frame, main, pagination.currentIndex)
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
    case _: tui.crossterm.KeyCode.Backspace =>
      previousView.map(ChangeTo.apply).getOrElse(Keep)
    case _: tui.crossterm.KeyCode.Enter =>
      paginatedList.selected
        .map(site => ChangeTo(nextView(Site(site))))
        .getOrElse(Keep)
    case _: tui.crossterm.KeyCode.Down  => paginatedList.nextItem(); Keep
    case _: tui.crossterm.KeyCode.Up    => paginatedList.previousItem(); Keep
    case _: tui.crossterm.KeyCode.Right => paginatedList.nextPage(); Keep
    case _: tui.crossterm.KeyCode.Left  => paginatedList.previousPage(); Keep
    case _                              => Keep

  private def renderSiteSelection(
      frame: Frame,
      area: Rect,
      selected: Option[Int]
  ): Unit =
    val items = sites
      .map((site) => ListWidget.Item(Text(Array(Spans.nostyle(site)))))

    val widget = ListWidget(
      items = items,
      block = Some(
        BlockWidget(
          borders = Borders.NONE,
          title = Some(
            Spans.styled("Select a site", Style(fg = Some(Color.White)))
          )
        )
      ),
      highlight_style = Style(
        fg = Some(context.theme.primaryColor),
        add_modifier = Modifier.BOLD
      )
    )

    frame.render_stateful_widget(widget, area)(State(selected = selected))

object ChooseSiteView:
  private val keyBindsNav = KeybindsNav(
    List(
      "↑ up",
      "↓ down",
      "← previous page",
      "→ next page",
      "enter select",
      "backspace go back",
      "q quit"
    )
  )

  private val sites = Array("nyaa", "mangakakalot", "mangadex")
