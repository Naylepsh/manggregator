package tui.views.assetmanagement

import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.Asset
import library.services.Assets
import tui.views.{View, showPrompt}
import tui.prompts.AssetPrompts.{Item, createItemsPrompt}
import tui.prompts.MenuPrompt

class MainAssetManagementView[F[_]: Sync: Console](
    prompt: ConsolePrompt,
    goBack: View[F],
    assetsService: Assets[F]
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
    "create" -> MenuPrompt.Action(
      text = "Create a new asset:",
      handle = _ => new CreateAssetView[F](prompt, this, assetsService).view()
    ),
    "edit" -> MenuPrompt.Action(
      text = "Edit an existing asset:",
      handle = _ =>
        assetsService.findAll().flatMap { assets =>
          new EditAssetsView[F](prompt, assets, this, assetsService).view()
        }
    )
  )

  private val menuPrompt = MenuPrompt.make(
    "manage-assets",
    "Choose an action:",
    actions,
    goBack
  )
