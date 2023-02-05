package tui.views

import scala.jdk.CollectionConverters.*

import cats.Applicative
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.elements.PromptableElementIF
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import de.codeshelf.consoleui.prompt.{
  ConsolePrompt,
  ListResult,
  PromtResultItemIF
}
import library.domain.asset.Asset
import tui.prompts.asset.makeAssetNameMenu

class CrawlResultsView[F[_]: Sync: Console](
    context: Context[F],
    assets: List[Asset],
    previous: View[F]
) extends View[F]:

  def view(): F[Unit] =
    makeAssetNameMenu(
      context.prompt,
      "Select an asset to see recent releases of:",
      assets,
      onHandleAsset,
      previous
    )

  private def onHandleAsset(asset: Asset): F[Unit] =
    showAssetChapters(asset) >> view()

  private def showAssetChapters(asset: Asset): F[Unit] =
    asset.chapters.foldLeft(Applicative[F].unit) { (acc, chapter) =>
      acc *> Console[F].println(s"${chapter.no.value} | ${chapter.url.value}")
    }
