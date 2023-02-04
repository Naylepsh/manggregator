package tui.views.assetmanagement

import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.Asset
import library.services.{Assets, Pages}
import tui.prompts.AssetPrompts.createAssetNamePrompt
import tui.views.{Context, View, showPrompt}

class EditAssetsView[F[_]: Sync: Console](
    context: Context[F],
    assets: List[Asset],
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

  private val menuPrompt = createAssetNamePrompt(
    "edit-assets",
    "Choose an asset to edit",
    assets,
    handle = pickAssetToEdit,
    viewToGoBackTo = goBack
  )

  private def pickAssetToEdit(result: String) =
    assets
      .find(_.id.value.toString == result)
      .map { asset =>
        new AssetManagementView(
          context,
          asset,
          this
        ).view()
      }
      .getOrElse(Sync[F].unit)
