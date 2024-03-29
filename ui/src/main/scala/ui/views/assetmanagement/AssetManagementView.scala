package ui.views.assetmanagement

import cats.effect.IO
import cats.implicits.*
import tui.*
import tui.crossterm.KeyCode
import tui.widgets.{ BlockWidget, ListWidget }
import ui.components.KeybindsNav
import ui.core.*

class AssetManagementView(
    context: Context[IO],
    previousView: Option[View]
) extends View:
  private val actions = List(
    Action(
      "Add a new asset",
      () => ChangeTo(CreateAssetView(context, Some(this), this))
    ),
    Action(
      "Edit an existing asset",
      () =>
        ChangeTo(
          AssetsView(
            context,
            Some(this),
            context.dispatcher.unsafeRunSync(
              context.services.assets.findAll()
            )
          )
        )
    )
  )
  private val items = StatefulList(items = actions.toArray)

  private val keyBindsNav = KeybindsNav(
    List(
      "↑ up",
      "↓ down",
      "backspace go back",
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

    chunks match
      case Array(main, nav) =>
        renderMenu(frame, main)
        keyBindsNav.render(frame, nav)
      case _ =>

  override def handleInput(key: KeyCode): ViewResult = key match
    case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => Exit
    case _: tui.crossterm.KeyCode.Backspace =>
      previousView.map(ChangeTo.apply).getOrElse(Keep)
    case _: tui.crossterm.KeyCode.Down => items.next(); Keep
    case _: tui.crossterm.KeyCode.Up   => items.previous(); Keep
    case _: tui.crossterm.KeyCode.Enter =>
      items.state.selected
        .flatMap(actions.get)
        .map(_.onSelect())
        .getOrElse(Keep)

    case _ => Keep

  private def renderMenu(frame: Frame, area: Rect): Unit =
    val items0 = items.items
      .map {
        case (action) =>
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
          title = Some(Spans.nostyle("Asset management"))
        )
      ),
      highlight_style = Style(
        fg = Some(context.theme.primaryColor),
        add_modifier = Modifier.BOLD
      )
    )

    frame.render_stateful_widget(widget, area)(items.state)
