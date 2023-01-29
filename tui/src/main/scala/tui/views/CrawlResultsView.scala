package tui.views

import scala.jdk.CollectionConverters.*

import cats.Applicative
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.elements.PromptableElementIF
import de.codeshelf.consoleui.prompt.builder.ListPromptBuilder
import de.codeshelf.consoleui.prompt.{ConsolePrompt, ListResult, PromtResultItemIF}
import library.domain.asset.Asset

class CrawlResultsView[F[_]: Sync: Console](
    prompt: ConsolePrompt,
    assets: List[Asset],
    previous: View[F]
) extends View[F]:

  // TODO: Handle unsafe `get`s
  def view(): F[Unit] =
    for
      rawResult <- showPrompt(prompt, buildAssetNamesPrompt)
      result = getListPromptResult(rawResult.get(crawlResultsName).get)
      _ <- assets
        .find(_.id.value.toString == result)
        .map { asset =>
          showAssetChapters(asset) >> view()
        }
        .getOrElse(result match
          case `goBackId` => goBack()
          case _          => exit()
        )
    yield ()

  private def exit() = Sync[F].unit
  private val goBackId = "go-back"
  private def goBack() = previous.view()

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
      .newItem(goBackId)
      .text("back")
      .add()
      .newItem("exit")
      .text("exit")
      .add()
      .addPrompt()
      .build()

  private def showAssetChapters(asset: Asset): F[Unit] =
    asset.chapters.foldLeft(Applicative[F].unit) { (acc, chapter) =>
      acc *> Console[F].println(s"${chapter.no.value} | ${chapter.url.value}")
    }
