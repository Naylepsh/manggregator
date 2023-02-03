package tui.prompts

import cats.effect.kernel.Sync
import cats.implicits._
import tui.prompts.AssetPrompts.{Item, createItemsPrompt}
import tui.views.View

object MenuPrompt:
  case class Action[F[_]](text: String, handle: String => F[Unit])

  def make[F[_]: Sync](
      promptName: String,
      promptMessage: String,
      actions: Map[String, Action[F]],
      viewToGoBackTo: View[F]
  ) = createItemsPrompt(
    promptName,
    promptMessage,
    actions.map { case (key, action) =>
      Item(id = key, text = action.text)
    }.toList,
    (result) =>
      actions.get(result).map(_.handle(result)).getOrElse(Sync[F].unit),
    viewToGoBackTo
  )
