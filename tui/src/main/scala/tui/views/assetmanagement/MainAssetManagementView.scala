package tui.views.assetmanagement

import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.Asset
import tui.views.View
import tui.prompts.AssetPrompts.createAssetNamePrompt
import cats.Monad
import cats.effect.kernel.Sync
import tui.views.showPrompt
import cats.implicits._
import library.services.Assets
import cats.effect.std.Console

class MainAssetManagementView[F[_]: Sync: Console](
    prompt: ConsolePrompt,
    assets: List[Asset],
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

  private val menuPrompt = createAssetNamePrompt(
    "manage-asset-main",
    "Choose an asset to manage:",
    assets,
    handle = pickAssetToManage,
    viewToGoBackTo = goBack
  )

  private def pickAssetToManage(result: String) =
    assets
      .find(_.id.value.toString == result)
      .map { asset =>
        new AssetManagementView(prompt, asset, this, assetsService).view()
      }
      .getOrElse(Sync[F].unit)
