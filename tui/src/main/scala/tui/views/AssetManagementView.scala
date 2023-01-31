package tui.views

import library.domain.asset.Asset
import de.codeshelf.consoleui.prompt.ConsolePrompt

// TODO:
final class AssetManagementView[F[_]](
    prompt: ConsolePrompt,
    assets: List[Asset],
    goBack: View[F]
) extends View[F]:

  override def view(): F[Unit] = ???

  // private val assetsPromptName = "assets"
  // private def buildAssetNamesPrompt =
  //   addCommonItems(createAssetNamesPrompt(prompt, assetsPromptName, assets))
