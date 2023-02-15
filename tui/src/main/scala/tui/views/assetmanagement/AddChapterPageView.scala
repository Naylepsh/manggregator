package tui.views.assetmanagement

import scala.util.Try

import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import core.Url
import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.Asset
import library.domain.page.{CreateChaptersPage, PageUrl, Site}
import library.services.Pages
import tui.prompts.InputPrompts.getValidatedInput
import tui.prompts.list.getInputFromList
import tui.utils.retry.retryUntilSuccess
import tui.views.{Context, View}

class AddChapterPageView[F[_]: Sync: Console](
    context: Context[F],
    asset: Asset,
    viewToGoBackTo: View[F]
) extends View[F]:
  override def view(): F[Unit] =
    val promptBuilder = context.prompt.getPromptBuilder()
    for
      site <- getInputFromList(context.prompt, "Enter the site:", validSites)
        .map(value => Site(value))
      url <- retryUntilSuccess(getChapterPageInput())
      _ <- context.services.pages.create(
        CreateChaptersPage(site, url, asset.id)
      )
      _ <- viewToGoBackTo.view()
    yield ()

  // TODO: get this stuff from database? Or from main app?
  private val validSites = List("nyaa", "mangakakalot", "mangadex")

  private def getChapterPageInput() =
    getValidatedInput(
      context.prompt,
      "Enter the chapter url:",
      validateChapterUrl
    )
  private def validateChapterUrl(input: String): Either[String, PageUrl] =
    Either.cond(Url.isUrl(input), PageUrl(input), s"$input is not a valid url")
