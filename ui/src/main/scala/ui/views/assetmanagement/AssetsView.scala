package ui.views.assetmanagement
import cats.effect.IO
import cats.implicits._
import library.domain.asset.{Asset, UpdateAsset}
import tui._
import tui.crossterm.KeyCode
import tui.widgets.ListWidget.State
import tui.widgets.{BlockWidget, ListWidget}
import ui.components.{KeybindsNav, Pagination}
import ui.core._
import ui.views.assetmanagement.addpage.AddPageView

class AssetsView(
    context: Context[IO],
    previousView: Option[View],
    assets: List[Asset]
) extends View:

  private val assetCollection = assets.sortBy(_.name).toArray
  private val paginatedList = PaginatedList(assetCollection)
  private val keyBindsNav = KeybindsNav(
    List(
      "↑ up",
      "↓ down",
      "e enable",
      "d disable",
      "p add page",
      "backspace go back",
      "q quit"
    )
  )

  private val assetHeight = 2

  override def render(frame: Frame): Unit =
    if (assets.isEmpty)
      renderNoAssets(frame)
    else
      renderAssets(frame)

  override def handleInput(key: KeyCode): ViewResult = key match
    case char: tui.crossterm.KeyCode.Char if char.c() == 'q' => Exit
    case char: tui.crossterm.KeyCode.Char if char.c() == 'd' =>
      updateAsset(_.disable())
      Keep
    case char: tui.crossterm.KeyCode.Char if char.c() == 'e' =>
      updateAsset(_.enable())
      Keep
    case char: tui.crossterm.KeyCode.Char if char.c() == 'p' =>
      paginatedList.selected
        .map(asset => ChangeTo(AddPageView(context, asset.id, this)))
        .getOrElse(Keep)
    case _: tui.crossterm.KeyCode.Backspace =>
      previousView.map(ChangeTo.apply).getOrElse(Keep)
    case _: tui.crossterm.KeyCode.Down  => paginatedList.nextItem(); Keep
    case _: tui.crossterm.KeyCode.Up    => paginatedList.previousItem(); Keep
    case _: tui.crossterm.KeyCode.Right => paginatedList.nextPage(); Keep
    case _: tui.crossterm.KeyCode.Left  => paginatedList.previousPage(); Keep
    case _                              => Keep

  private def updateAsset(update: Asset => Asset) =
    paginatedList.selectedIndex.flatMap { index =>
      paginatedList.selected.map { asset =>
        val updatedValues = update(asset)
        val result = context.dispatcher.unsafeRunSync(
          context.services.assets
            .update(
              UpdateAsset(
                id = updatedValues.id,
                name = updatedValues.name,
                enabled = updatedValues.enabled
              )
            )
        )
        paginatedList.update(index, updatedValues)
      }
    }

  private def renderNoAssets(frame: Frame): Unit =
    val chunks = Layout(
      direction = Direction.Vertical,
      constraints = Array(
        Constraint.Percentage(90),
        Constraint.Percentage(10)
      )
    ).split(frame.size)

    chunks.toList match
      case main :: nav :: Nil =>
        renderNoAssetsMessage(frame, main)
        keyBindsNav.render(frame, nav)
      case _ =>

  private def renderNoAssetsMessage(frame: Frame, area: Rect): Unit =
    frame.render_widget(
      BlockWidget(title = Some(Spans.nostyle("No assets found"))),
      area
    )

  private def renderAssets(frame: Frame): Unit =
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
          assetHeight
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

  private def renderAssets(
      frame: Frame,
      area: Rect,
      assetsPage: Array[Asset],
      selected: Option[Int]
  ): Unit =
    val padding = " " * 3
    val items = assetsPage
      .map { case (asset) =>
        val style =
          if (asset.enabled.value)
            Style(fg = Some(Color.White))
          else
            Style(fg = Some(Color.DarkGray))

        val lines = Array(
          Spans.nostyle(""),
          Spans.styled(
            s"$padding${asset.name.value}",
            style
          )
        )

        ListWidget.Item(Text(lines))
      }

    val widget = ListWidget(
      items = items,
      block = Some(
        BlockWidget(
          borders = Borders.NONE,
          title = Some(
            Spans.from(
              Span.nostyle("Select an asset to see recent releases of"),
              Span.nostyle(s" - ${assets.length} assets")
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
