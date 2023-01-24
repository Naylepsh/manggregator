package tui.views

import library.domain.asset.Asset
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.Applicative
import cats.implicits._
import scala.jdk.CollectionConverters.*
import de.codeshelf.consoleui.prompt.ConsolePrompt
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import de.codeshelf.consoleui.prompt.PromtResultItemIF
import de.codeshelf.consoleui.prompt.ListResult
import de.codeshelf.consoleui.elements.PromptableElementIF

class CrawlResultsView[F[_]: Sync: Console](
    prompt: ConsolePrompt,
    assets: List[Asset]
) extends View[F]:

  // TODO: Handle unsafe `get`s
  def view(): F[Unit] =
    for
      rawResult <- showPrompt(buildAssetNamesPrompt)
      assetId = getListPromptResult(rawResult.get(crawlResultsName).get)
      asset = assets.find(_.id.value.toString == assetId).get
      _ <- showAssetChapters(asset)
    yield ()

  private val crawlResultsName = "crawl-results"

  private def buildAssetNamesPrompt =
    val promptBuilder = prompt.getPromptBuilder()

    val header = promptBuilder
      .createListPrompt()
      .name(crawlResultsName)
      .message("Select an asset to see recent releases of:")

    assets
      .foldLeft(header) { (builder, asset) =>
        builder.newItem(asset.id.value.toString).text(asset.name.value).add()
      }
      .addPrompt()
      .build()

  private def showAssetChapters(asset: Asset): F[Unit] =
    asset.chapters.foldLeft(Applicative[F].unit) { (acc, chapter) =>
      acc *> Console[F].println(s"${chapter.no} | ${chapter.url}")
    }

  // --- Console UI helpers ---
  // TODO: Move to separate helper package

  private def getListPromptResult(rawPromptResult: PromtResultItemIF): String =
    rawPromptResult.asInstanceOf[ListResult].getSelectedId()

  private def showPrompt(
      prompts: java.util.List[PromptableElementIF]
  ): F[scala.collection.mutable.Map[String, ? <: PromtResultItemIF]] =
    Applicative[F].pure {
      prompt.prompt(prompts).asScala
    }
