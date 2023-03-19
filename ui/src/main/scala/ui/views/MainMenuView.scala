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
import ui.core.Action
import ui.views.assetmanagement.AssetManagementView
import ui.components.KeybindsNav

class MainMenuView(context: Context[IO])(using IORuntime) extends View:
  import MainMenuView._
  private var state: ViewState = ViewState.Ready

  private val actions = List(
    Action("Trigger a crawl", triggerCrawl),
    Action("Browse the recent releases", browseRecentReleases),
    Action("Manage the assets", manageAssets)
  )
  private val items = StatefulList(items = actions.toArray)

  private val keyBindsNav = KeybindsNav(
    List(
      "↑ up",
      "↓ down",
      "q quit"
    )
  )

  override def render(frame: Frame): Unit =
    this.state match
      case ViewState.Loading =>
        val chunks = Layout(
          direction = Direction.Horizontal,
          constraints = Array(Constraint.Percentage(100))
        ).split(frame.size)
        chunks match
          case Array(main) =>
            renderWaitingForCrawlToFinish(frame, main)
          case _ =>

      case ViewState.Ready =>
        val chunks = Layout(
          direction = Direction.Vertical,
          constraints =
            Array(Constraint.Percentage(90), Constraint.Percentage(10))
        ).split(frame.size)
        chunks match
          case Array(menu, nav) =>
            renderMenu(frame, menu)
            keyBindsNav.render(frame, nav)
          case _ =>

  override def handleInput(key: KeyCode): ViewResult =
    this.state match
      case ViewState.Loading =>
        key match
          case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => Exit
          case _                                                   => Keep
      case ViewState.Ready =>
        key match
          case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => Exit
          case _: tui.crossterm.KeyCode.Down => items.next(); Keep
          case _: tui.crossterm.KeyCode.Up   => items.previous(); Keep
          case _: tui.crossterm.KeyCode.Enter =>
            items.state.selected
              .flatMap(actions.get)
              .map(_.onSelect())
              .getOrElse(Keep)
          case _ => Keep

  private def renderWaitingForCrawlToFinish(
      frame: Frame,
      area: Rect
  ): Unit =
    val widget = BlockWidget(
      title = Some(Spans.nostyle("Crawling. This can take a moment..."))
    )
    frame.render_widget(widget, area)

  private def renderMenu(frame: Frame, area: Rect): Unit =
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

    frame.render_stateful_widget(widget, area)(items.state)

  private def triggerCrawl(): ViewResult =
    (IO(this.state = ViewState.Loading)
      >> context.services.crawler
        .crawl()
        .run(context.services.crawlerLibrary)
        .flatTap(_ => IO(this.state = ViewState.Ready)))
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

  private def manageAssets(): ViewResult =
    ChangeTo(AssetManagementView(context, Some(this)))

object MainMenuView:
  sealed trait ViewState
  object ViewState:
    object Loading extends ViewState
    object Ready extends ViewState
