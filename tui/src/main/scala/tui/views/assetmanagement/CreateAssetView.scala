package tui.views.assetmanagement

import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.{AssetName, CreateAsset, Enabled}
import library.services.Assets
import tui.prompts.InputPrompts.getInput
import tui.views.View

class CreateAssetView[F[_]: Sync: Console](
    prompt: ConsolePrompt,
    goBack: View[F],
    assets: Assets[F]
) extends View[F]:
  override def view(): F[Unit] =
    val promptBuilder = prompt.getPromptBuilder()
    for
      name <- getNameInput()
      _ <- assets
        .create(
          CreateAsset(name = AssetName(name), enabled = Enabled(true))
        )
        .flatMap(_ match
          case Left(reason) =>
            Console[F].println(s"Could not create, because: $name")
          case Right(id) => Console[F].println(s"Successfully created $name")
        )
      _ <- goBack.view()
    yield ()

  private def getNameInput() = getInput(prompt, "Enter the name:")
