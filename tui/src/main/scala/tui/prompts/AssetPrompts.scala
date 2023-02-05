package tui.prompts

import cats.Monad
import de.codeshelf.consoleui.prompt.ConsolePrompt
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import library.domain.asset.Asset
import tui.prompts.handlers._
import tui.views.View
import cats.implicits._

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
    createItemsPrompt(
      promptName,
      promptMessage,
      assets
        .sortBy(_.name.value)
        .map(asset =>
          Item(id = asset.id.value.toString, text = asset.name.value)
        ),
      handle,
      viewToGoBackTo
    )

  case class Item(id: String, text: String)

  def createItemsFormPrompt[F[_]: Monad](
      promptMessage: String,
      items: List[String]
  ) =
    val subHandler =
      createItemsSubHandler(items.map(i => Item(i, i)), handle = _.pure)

    makeListHandler(
      List(subHandler),
      "list-form-input",
      promptMessage
    )

  def createItemsPrompt[F[_]: Monad](
      promptName: String,
      promptMessage: String,
      items: List[Item],
      handle: String => F[Unit],
      viewToGoBackTo: View[F]
  ) =
    val subHandlers = List(
      createItemsSubHandler(items, handle),
      goBackHandler(viewToGoBackTo),
      exitHandler()
    )

    makeListHandler(
      subHandlers,
      promptName,
      promptMessage
    )

  def createItemsSubHandler[F[_], Output](
      items: List[Item],
      handle: String => F[Output]
  ): SinglePropHandler[F, ListPromptBuilder, Output] =
    SinglePropHandler[F, ListPromptBuilder, Output](
      addToPrompt = (builder) =>
        items
          .foldLeft(builder) { (builder, item) =>
            builder
              .newItem(item.id)
              .text(item.text)
              .add()
          },
      check = (result) => items.find(_.id == result).isDefined,
      handle = handle
    )
