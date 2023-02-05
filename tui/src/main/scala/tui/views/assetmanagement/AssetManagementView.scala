package tui.views.assetmanagement

import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.{Asset, AssetDoesNotExist, UpdateAsset}
import library.services.{Assets, Pages}
import tui.prompts.AssetPrompts.{Item, createItemsPrompt}
import tui.prompts.menu
import tui.views.{Context, View, showPrompt}

class AssetManagementView[F[_]: Sync: Console](
    context: Context[F],
    asset: Asset,
    goBack: View[F]
) extends View[F]:
  override def view(): F[Unit] =
    menu.make(context.prompt, "Pick an action:", actions, goBack)

  private val actions = Map(
    "toggle" -> inferToggle(),
    "add-pages" -> goToAddChapterPageView()
  )

  private def inferToggle(): menu.Action[F] =
    val (text, handle) =
      if (asset.enabled.value)
        ("Disable", () => update(asset.disable()))
      else
        ("Enable", () => update(asset.enable()))
    menu.Action(
      text,
      _ =>
        handle().flatMap(_ match
          case Left(value) =>
            Console[F].println(s"Could not disable due to $value")
          case Right(value) => goBack.view()
        )
    )

  private def update(asset: Asset): F[Either[AssetDoesNotExist, Unit]] =
    context.services.assets
      .update(
        UpdateAsset(
          id = asset.id,
          name = asset.name,
          enabled = asset.enabled
        )
      )

  private def goToAddChapterPageView(): menu.Action[F] =
    menu.Action(
      text = "Add chapters page",
      handle = _ => new AddChapterPageView[F](context, asset, this).view()
    )
