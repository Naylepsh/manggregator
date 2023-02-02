package tui.views.assetmanagement

import cats.effect.kernel.Sync
import de.codeshelf.consoleui.prompt.ConsolePrompt
import tui.views.View
import tui.prompts.InputPrompts.getInput
import cats.implicits._
import cats.effect.std.Console
import library.services.Assets
import library.domain.asset.CreateAsset
import library.domain.asset.AssetName
import library.domain.asset.Enabled

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

  // TODO: use for sites validation
  private def getValidatedInput[A](
      message: String,
      validate: String => Either[Throwable, A]
  ) =
    getInput(prompt, message).flatMap { input =>
      validate(input) match
        case Left(reason) => Console[F].println(reason) *> reason.asLeft.pure
        case Right(value) => value.asRight[Throwable].pure
    }
