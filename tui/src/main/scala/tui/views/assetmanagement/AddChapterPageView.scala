package tui.views.assetmanagement

import java.net.URI

import scala.util.Try

import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.asset.Asset
import library.domain.page.{CreateChaptersPage, PageUrl, Site}
import library.services.Pages
import tui.prompts.InputPrompts.getValidatedInput
import tui.prompts.AssetPrompts.createItemsFormPrompt
import tui.utils.retry.retryUntilSuccess
import tui.views.{Context, View, showPrompt}

class AddChapterPageView[F[_]: Sync: Console](
    context: Context[F],
    asset: Asset,
    viewToGoBackTo: View[F]
) extends View[F]:
  override def view(): F[Unit] =
    val promptBuilder = context.prompt.getPromptBuilder()
    for
      rawSite <- showPrompt(
        context.prompt,
        getSite.combinePrompts(promptBuilder)
      )
      site <- getSite.handle(rawSite).map(_.map(Site.apply).get)
      url <- retryUntilSuccess(getChapterPageInput())
      _ <- context.services.pages.create(
        CreateChaptersPage(site, url, asset.id)
      )
      _ <- viewToGoBackTo.view()
    yield ()

  // TODO: get this stuff from database? Or from main app?
  private val validSites = List("nyaa", "mangakakalot", "mangadex")
  private val getSite = createItemsFormPrompt("Enter the site:", validSites)

  private def getChapterPageInput() =
    getValidatedInput(
      context.prompt,
      "Enter the chapter url:",
      validateChapterUrl
    )
  private def validateChapterUrl(input: String): Either[String, PageUrl] =
    Either.cond(isUrl(input), PageUrl(input), s"$input is not a valid url")

  private def isUrl(s: String): Boolean =
    Try {
      // Just constructing URI is not enough to validate.
      // Even `new URI("string")` passes.
      // toURL has to be called.
      new URI(s).toURL()
    }.isSuccess
