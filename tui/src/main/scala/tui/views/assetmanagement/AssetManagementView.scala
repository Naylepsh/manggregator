package tui.views.assetmanagement

import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.{Asset, AssetDoesNotExist, UpdateAsset}
import library.services.Assets
import tui.prompts.AssetPrompts.{Item, createItemsPrompt}
import tui.views.{View, showPrompt}

class AssetManagementView[F[_]: Sync: Console](
    prompt: ConsolePrompt,
    asset: Asset,
    goBack: View[F],
    assetsService: Assets[F]
) extends View[F]:
  import AssetManagementView._

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
    "toggle" -> inferToggle(assetsService, asset, goBack)
  )

  private val menuPrompt = createItemsPrompt(
    "manage-asset",
    "Choose an asset to manage:",
    actions.map { case (key, action) =>
      Item(id = key, text = action.text)
    }.toList,
    (result) =>
      actions.get(result).map(_.handle(result)).getOrElse(Sync[F].unit),
    goBack
  )

object AssetManagementView:
  private case class Action[F[_]](text: String, handle: String => F[Unit])

  private def inferToggle[F[_]: Monad: Console](
      assets: Assets[F],
      asset: Asset,
      goBack: View[F]
  ): Action[F] =
    val (text, handle) =
      if (asset.enabled.value)
        ("Disable", () => update(assets, asset.disable()))
      else
        ("Enable", () => update(assets, asset.enable()))
    Action(
      text,
      _ =>
        handle().flatMap(_ match
          case Left(value) =>
            Console[F].println(s"Could not disable due to $value")
          case Right(value) => goBack.view()
        )
    )

  private def update[F[_]](
      assets: Assets[F],
      asset: Asset
  ): F[Either[AssetDoesNotExist, Unit]] =
    assets
      .update(
        UpdateAsset(
          id = asset.id,
          name = asset.name,
          enabled = asset.enabled
        )
      )
