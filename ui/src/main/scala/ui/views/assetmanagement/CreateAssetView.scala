package ui.views.assetmanagement

import cats.effect.IO
import library.domain.asset.*
import tui.*
import tui.crossterm.KeyCode
import tui.widgets.BlockWidget
import ui.components.KeybindsNav
import ui.core.*
import ui.views.common.InputView

class CreateAssetView(
    context: Context[IO],
    previousView: Option[View],
    next: View
) extends View:
  import CreateAssetView.*

  val inputView =
    InputView(validateTitle, "Asset title", createAsset, previousView)

  override def render(frame: Frame): Unit = inputView.render(frame)

  override def handleInput(key: KeyCode): ViewResult =
    inputView.handleInput(key)

  private def createAsset(name: String): View =
    context.dispatcher.unsafeRunSync(
      context.services.assets
        .create(
          CreateAsset(name = AssetName(name), enabled = Enabled(true))
        )
        .map(PostCreationAssetView(context, _, next))
    )

object CreateAssetView:
  def validateTitle(input: String): Either[String, String] =
    Either.cond(input.length > 0, input, "Empty title is invalid")

private class PostCreationAssetView(
    context: Context[IO],
    creationResult: Either[AssetAlreadyExists, AssetId],
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
      reason => s"Failed to create the asset due to: ${reason.toString}",
      assetId => s"Successfully created asset with id: ${assetId.value}"
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
