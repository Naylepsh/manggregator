package tui.views.assetmanagement

import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.Asset
import library.services.{Assets, Pages}
import tui.views.{Context, View, showPrompt}
import tui.prompts.asset.makeAssetNameMenu

class EditAssetsView[F[_]: Sync: Console](
    context: Context[F],
    assets: List[Asset],
    goBack: View[F]
) extends View[F]:

  override def view(): F[Unit] =
    makeAssetNameMenu(
      context.prompt,
      "Choose an asset to edit:",
      assets,
      pickAssetToEdit,
      goBack
    )

  private def pickAssetToEdit(asset: Asset) =
    new AssetManagementView(
      context,
      asset,
      this
    ).view()
