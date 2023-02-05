package tui.prompts

import cats.effect.kernel.Sync
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import library.domain.asset.Asset
import tui.prompts.handlers._
import tui.prompts.menu
import tui.views.View

object asset:
  def makeBuildReleasesMenu[F[_]: Sync](
      prompt: ConsolePrompt,
      assets: List[Asset],
      handle: Asset => F[Unit],
      viewToGoBackTo: View[F]
  ) =
    makeAssetNameMenu(
      prompt,
      "Select an asset to see recent releases of:",
      assets,
      handle,
      viewToGoBackTo
    )

  def makeAssetNameMenu[F[_]: Sync](
      prompt: ConsolePrompt,
      message: String,
      assets: List[Asset],
      handle: Asset => F[Unit],
      viewToGoBackTo: View[F]
  ): F[Unit] =
    val actions = assets
      .map(asset =>
        asset.id.value.toString -> menu
          .Action(text = asset.name.value, handle = _ => handle(asset))
      )
      .toMap

    menu.make(prompt, message, actions, viewToGoBackTo)
