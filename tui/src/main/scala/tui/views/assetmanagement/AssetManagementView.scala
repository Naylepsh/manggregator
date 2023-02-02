package tui.views.assetmanagement

import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.{Asset, AssetDoesNotExist, UpdateAsset}
import library.services.Assets
import tui.prompts.AssetPrompts.{Item, createItemsPrompt}
import tui.prompts.MenuPrompt
import tui.views.{View, showPrompt}

class AssetManagementView[F[_]: Sync: Console](
    prompt: ConsolePrompt,
    asset: Asset,
    goBack: View[F],
    assets: Assets[F]
) extends View[F]:
  override def view(): F[Unit] =
    val promptBuilder = prompt.getPromptBuilder()
    for
      rawResult <- showPrompt(
        prompt,
        menuPrompt.combinePrompts(promptBuilder)
      )
      _ <- menuPrompt.handle(rawResult).map(_.getOrElse(()))
    yield ()

  private val actions = Map(
    "toggle" -> inferToggle()
  )

  private val menuPrompt = MenuPrompt.make(
    "manage-asset",
    "Choose an asset to manage:",
    actions,
    goBack
  )

  private def inferToggle(): MenuPrompt.Action[F] =
    val (text, handle) =
      if (asset.enabled.value)
        ("Disable", () => update(asset.disable()))
      else
        ("Enable", () => update(asset.enable()))
    MenuPrompt.Action(
      text,
      _ =>
        handle().flatMap(_ match
          case Left(value) =>
            Console[F].println(s"Could not disable due to $value")
          case Right(value) => goBack.view()
        )
    )

  private def update(asset: Asset): F[Either[AssetDoesNotExist, Unit]] =
    assets
      .update(
        UpdateAsset(
          id = asset.id,
          name = asset.name,
          enabled = asset.enabled
        )
      )
