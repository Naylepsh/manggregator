package ui.views.crawlresults

import cats.effect.IO
import cats.implicits.*
import library.domain.asset.Asset
import library.domain.chapter.Chapter
import tui.*
import tui.crossterm.KeyCode
import tui.widgets.ListWidget.State
import tui.widgets.{ BlockWidget, ListWidget }
import ui.components.{ KeybindsNav, Pagination }
import ui.core.*

class ChaptersView(
    context: Context[IO],
    asset: Asset,
    chapters: List[Chapter],
    previousView: Option[View]
) extends View:

  private val results       = chapters.sortBy(_.dateReleased)
  private val paginatedList = PaginatedList(results.toArray)
  private val keyBindsNav = KeybindsNav(
    List(
      "↑ up",
      "↓ down",
      "← previous page",
      "→ next page",
      "s mark as seen",
      "backspace go back",
      "q quit"
    )
  )
  private val chapterItemHeight = 3

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
          chapterItemHeight
        )

        renderChapters(
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
    case char: tui.crossterm.KeyCode.Char if char.c() == 's' =>
      markCurrentlySelectedAsSeen()
      Keep
    case _: tui.crossterm.KeyCode.Backspace =>
      previousView.map(ChangeTo.apply).getOrElse(Keep)
    case _: tui.crossterm.KeyCode.Down  => paginatedList.nextItem(); Keep
    case _: tui.crossterm.KeyCode.Up    => paginatedList.previousItem(); Keep
    case _: tui.crossterm.KeyCode.Right => paginatedList.nextPage(); Keep
    case _: tui.crossterm.KeyCode.Left  => paginatedList.previousPage(); Keep
    case _                              => Keep

  private def markCurrentlySelectedAsSeen(): Unit =
    paginatedList.selectedIndex
      .flatMap(results.get)
      .map(chapter =>
        context.dispatcher.unsafeRunSync(
          context.services.chapters.markAsSeen(List(chapter.id))
        )
      )

  private def renderChapters(
      frame: Frame,
      area: Rect,
      chaptersSubset: Array[Chapter],
      selected: Option[Int]
  ): Unit =
    val padding = " " * 3
    val chapterListItems = chaptersSubset
      .map {
        case (chapter) =>
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
              Span.nostyle(s" - ${results.length} chapters")
            )
          )
        )
      ),
      highlight_style = Style(
        fg = Some(context.theme.primaryColor),
        add_modifier = Modifier.BOLD
      )
    )

    val state = State(selected = selected)
    frame
      .render_stateful_widget(chapterWidget, area)(state)
