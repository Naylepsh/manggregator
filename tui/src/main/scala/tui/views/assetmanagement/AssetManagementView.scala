package tui.views.assetmanagement

import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.Asset
import tui.views.View
import tui.prompts.AssetPrompts.{Item, createItemsPrompt}
import cats.Monad
import cats.effect.kernel.Sync
import tui.views.showPrompt
import cats.implicits._
import library.services.Assets
import library.domain.asset.UpdateAsset
import cats.effect.std.Console
import cats.implicits._

class AssetManagementView[F[_]: Sync: Console](
    prompt: ConsolePrompt,
    asset: Asset,
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

  private case class Action(text: String, handle: String => F[Unit])
  private val actions = Map(
    "disable" -> Action(
      text = "Disable",
      handle = _ => disableAsset() >> view()
    )
  )

  private def disableAsset(): F[Unit] =
    val disabledAsset = asset.disable()
    assetsService
      .update(
        UpdateAsset(
          id = disabledAsset.id,
          name = disabledAsset.name,
          enabled = disabledAsset.enabled
        )
      )
      .flatMap(_ match
        case Left(value) =>
          Console[F].println(s"Could not disable due to $value")
        case Right(value) => goBack.view()
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
