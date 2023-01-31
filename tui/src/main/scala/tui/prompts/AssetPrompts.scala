package tui.prompts

import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.Asset
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import tui.prompts.handlers._
import cats.Monad
import tui.views.View

object AssetPrompts:
  def buildReleasesPrompt[F[_]: Monad](
      assets: List[Asset],
      handle: String => F[Unit],
      viewToGoBackTo: View[F]
  ) =
    createAssetNamePrompt(
      "asset-releases",
      "Select an asset to see recent releases of:",
      assets,
      handle,
      viewToGoBackTo
    )

  def createAssetNamePrompt[F[_]: Monad](
      promptName: String,
      promptMessage: String,
      assets: List[Asset],
      handle: String => F[Unit],
      viewToGoBackTo: View[F]
  ) =
    val subHandlers = List(
      createAssetNamesSubHandler(assets, handle),
      goBackHandler(viewToGoBackTo),
      exitHandler()
    )

    makeListHandler(
      subHandlers,
      promptName,
      promptMessage
    )

  def createAssetNamesSubHandler[F[_]](
      assets: List[Asset],
      handle: String => F[Unit]
  ): SinglePropHandler[F, ListPromptBuilder] =
    SinglePropHandler[F, ListPromptBuilder](
      addToPrompt = (builder) =>
        assets
          .foldLeft(builder) { (builder, asset) =>
            builder
              .newItem(asset.id.value.toString)
              .text(asset.name.value)
              .add()
          },
      check = (result) => assets.find(_.id.value.toString == result).isDefined,
      handle = handle
    )
