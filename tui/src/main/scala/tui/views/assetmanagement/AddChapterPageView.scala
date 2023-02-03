package tui.views.assetmanagement

import de.codeshelf.consoleui.prompt.ConsolePrompt
import cats.effect.kernel.Sync
import tui.views.View
import library.domain.asset.Asset
import library.services.Pages
import tui.prompts.InputPrompts.getValidatedInput
import library.domain.page.Site
import cats.effect.std.Console
import library.domain.page.PageUrl
import scala.util.Try
import java.net.URI
import tui.utils.retry.retryUntilSuccess
import cats.implicits._
import library.domain.page.CreateChaptersPage

class AddChapterPageView[F[_]: Sync: Console](
    prompt: ConsolePrompt,
    asset: Asset,
    pages: Pages[F],
    viewToGoBackTo: View[F]
) extends View[F]:
  override def view(): F[Unit] =
    for
      site <- retryUntilSuccess(getSiteInput())
      url <- retryUntilSuccess(getChapterPageInput())
      _ <- pages.create(CreateChaptersPage(site, url, asset.id))
      _ <- viewToGoBackTo.view()
    yield ()

  private def getSiteInput() =
    getValidatedInput(prompt, "Enter the site:", validateSite)
  private def validateSite(input: String): Either[String, Site] =
    Either.cond(
      validSites.contains(input),
      Site(input),
      s"$input is not a supported site"
    )

  // TODO: get this stuff from database? Or from main app?
  private val validSites = List("nyaa", "mangakakalot", "mangadex")

  private def getChapterPageInput() =
    getValidatedInput(prompt, "Enter the chapter url:", validateChapterUrl)
  private def validateChapterUrl(input: String): Either[String, PageUrl] =
    Either.cond(isUrl(input), PageUrl(input), s"$input is not a valid url")

  private def isUrl(s: String): Boolean =
    Try {
      // Just constructing URI is not enough to validate.
      // Even `new URI("string")` passes.
      // toURL has to be called.
      new URI(s).toURL()
    }.isSuccess
