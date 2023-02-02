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
    "create" -> Action(
      text = "Create a new asset:",
      handle = _ => new CreateAssetView[F](prompt, this, assetsService).view()
    ),
    "edit" -> Action(
      text = "Edit an existing asset:",
      handle = _ =>
        assetsService.findAll().flatMap { assets =>
          new EditAssetsView[F](prompt, assets, this, assetsService).view()
        }
    )
  )

  private case class Action[F[_]](text: String, handle: String => F[Unit])

  private val menuPrompt = createItemsPrompt(
    "manage-assets",
    "Choose an action:",
    actions.map { case (key, action) =>
      Item(id = key, text = action.text)
    }.toList,
    (result) =>
      actions.get(result).map(_.handle(result)).getOrElse(Sync[F].unit),
    goBack
  )
