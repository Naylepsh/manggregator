package ui.views.assetmanagement.addpage

import cats.effect.IO
import core.Url
import library.domain.asset.AssetId
import library.domain.page.*
import tui.*
import tui.crossterm.KeyCode
import tui.widgets.BlockWidget
import ui.components.KeybindsNav
import ui.core.*
import ui.views.common.InputView

class AddPageView(context: Context[IO], assetId: AssetId, previousView: View)
    extends View:

  private def chooseUrlView(site: Site) = InputView(
    url => Url.valid(url).map(url => PageUrl(url.toString)),
    s"Enter url on ${site.value} site",
    addChapterPage(site),
    Some(previousView)
  )

  private val view = ChooseSiteView(context, chooseUrlView, Some(previousView))

  override def render(frame: Frame): Unit = view.render(frame)

  override def handleInput(key: KeyCode): ViewResult = view.handleInput(key)

  private def addChapterPage(site: Site)(url: PageUrl): View =
    context.dispatcher.unsafeRunSync(
      context.services.pages
        .create(CreateChaptersPage(site, url, assetId))
        .map(PostPageCreationView(context, _, previousView))
    )

private class PostPageCreationView(
    context: Context[IO],
    creationResult: Either[PageAlreadyExists, PageId],
    postConfirmationView: View
) extends View:

  private val keyBindsNav = KeybindsNav(
    List(
      "enter confirm",
      "q quit"
    )
  )

  override def render(frame: Frame): Unit =
    val chunks = Layout(
      direction = Direction.Vertical,
      constraints = Array(
        Constraint.Percentage(90),
        Constraint.Percentage(10)
      )
    ).split(frame.size)

    val text = creationResult.fold(
      reason => s"Failed to add the page due to: ${reason.toString}",
      assetId => s"Successfully added the page with id: ${assetId.value}"
    )

    chunks match
      case Array(main, nav) =>
        renderConfirmation(frame, main, text)
        keyBindsNav.render(frame, nav)
      case _ =>

  private def renderConfirmation(
      frame: Frame,
      area: Rect,
      text: String
  ): Unit =
    val widget = BlockWidget(
      title = Some(Spans.nostyle(text))
    )
    frame.render_widget(widget, area)

  override def handleInput(key: KeyCode): ViewResult =
    key match
      case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => Exit
      case _: tui.crossterm.KeyCode.Enter                      => ChangeTo(postConfirmationView)
      case _                                                   => Keep
