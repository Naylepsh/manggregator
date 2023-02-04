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
import tui.prompts.AssetPrompts.buildReleasesPrompt

class CrawlResultsView[F[_]: Sync: Console](
    context: Context[F],
    assets: List[Asset],
    previous: View[F]
) extends View[F]:

  def view(): F[Unit] =
    val promptBuilder = context.prompt.getPromptBuilder()
    for
      rawResult <- showPrompt(
        context.prompt,
        releasesPrompt.combinePrompts(promptBuilder)
      )
      _ <- releasesPrompt.handle(rawResult).map(_.getOrElse(()))
    yield ()

  private val releasesPrompt =
    buildReleasesPrompt(assets, onHandleAsset, previous)

  private def onHandleAsset(result: String): F[Unit] =
    assets
      .find(_.id.value.toString == result)
      .map { asset => showAssetChapters(asset) >> view() }
      .getOrElse(exit())

  private def exit() = Sync[F].unit

  private def showAssetChapters(asset: Asset): F[Unit] =
    asset.chapters.foldLeft(Applicative[F].unit) { (acc, chapter) =>
      acc *> Console[F].println(s"${chapter.no.value} | ${chapter.url.value}")
    }
