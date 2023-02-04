package tui.views.assetmanagement

import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.Asset
import library.services.{Assets, Pages}
import tui.prompts.AssetPrompts.{Item, createItemsPrompt}
import tui.prompts.MenuPrompt
import tui.views.{Context, View, showPrompt}

class MainAssetManagementView[F[_]: Sync: Console](
    context: Context[F],
    goBack: View[F]
) extends View[F]:

  override def view(): F[Unit] =
    val promptBuilder = context.prompt.getPromptBuilder()
    for
      rawResult <- showPrompt(
        context.prompt,
        menuPrompt.combinePrompts(promptBuilder)
      )
      _ <- menuPrompt.handle(rawResult).map(_.getOrElse(()))
    yield ()

  private val actions = Map(
    "create" -> MenuPrompt.Action(
      text = "Create a new asset",
      handle = _ => new CreateAssetView[F](context, this).view()
    ),
    "edit" -> MenuPrompt.Action(
      text = "Edit an existing asset",
      handle = _ =>
        context.services.assets.findAll().flatMap { assets =>
          new EditAssetsView[F](
            context,
            assets,
            this
          ).view()
        }
    )
  )

  private val menuPrompt = MenuPrompt.make(
    "manage-assets",
    "Choose an action:",
    actions,
    goBack
  )
