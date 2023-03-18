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
import ui.views.crawlresults.CrawlResultsView
import ui.core.Context
import cats.effect.IO
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._
import library.domain.chapter.DateReleased
import cats.effect.unsafe.IORuntime
import ui.views.common.DateInputView
import java.util.Date

class MainMenuView(context: Context[IO])(using IORuntime) extends View:
  private var isLoading = false

  private case class Action(label: String, onSelect: () => ViewResult)

  private val actions = List(
    Action("Trigger a crawl", triggerCrawl),
    Action("Browse the recent releases", browseRecentReleases),
    Action("Manage the assets", () => Keep)
  )

  private def triggerCrawl(): ViewResult =
    (IO(this.isLoading = true)
      >> context.services.crawler
        .crawl()
        .run(context.services.crawlerLibrary)
        .flatTap(_ => IO(this.isLoading = false)))
      .unsafeRunAndForget()

    Keep

  private def browseRecentReleases(): ViewResult =
    ChangeTo(
      DateInputView(date =>
        context.services.assets
          .findRecentReleases(DateReleased(date))
          .map(crawlResults =>
            CrawlResultsView(context, crawlResults, Some(this))
          )
          .unsafeRunSync()
      )
    )

  private val items = StatefulList(items = actions.toArray)

  private def renderWaitingForCrawlToFinish(
      frame: Frame,
      layout: Layout
  ): Unit =
    val widget = BlockWidget(
      title = Some(Spans.nostyle("Crawling. This can take a moment..."))
    )
    frame.render_widget(widget, layout.split(frame.size).head)

  private def renderMenu(frame: Frame, layout: Layout): Unit =
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
      highlight_style = Style(
        fg = Some(context.theme.primaryColor),
        add_modifier = Modifier.BOLD
      )
    )

    frame
      .render_stateful_widget(widget, layout.split(frame.size).head)(
        items.state
      )

  override def render(frame: Frame): Unit =
    val layout = Layout(
      direction = Direction.Horizontal,
      constraints = Array(Constraint.Percentage(100))
    )

    if (this.isLoading)
      renderWaitingForCrawlToFinish(frame, layout)
    else
      renderMenu(frame, layout)

  override def handleInput(key: KeyCode): ViewResult = key match
    case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => Exit
    case _: tui.crossterm.KeyCode.Down => items.next(); Keep
    case _: tui.crossterm.KeyCode.Up   => items.previous(); Keep
    case _: tui.crossterm.KeyCode.Enter =>
      items.state.selected
        .flatMap(actions.get)
        .map(_.onSelect())
        .getOrElse(Keep)

    case _ => Keep
