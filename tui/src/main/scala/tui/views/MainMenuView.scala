package tui.views

import java.text.SimpleDateFormat
import java.util.Date

import scala.jdk.CollectionConverters.*
import scala.util.Try

import cats.Applicative
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import crawler.domain.Library
import crawler.services.Crawler
import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.domain.chapter.DateReleased
import library.services.{Assets, Pages}
import tui.prompts.InputPrompts.getInput
import tui.utils.retry.retryUntilSuccess
import tui.views.assetmanagement.MainAssetManagementView

class MainMenuView[F[_]: Console: Sync](
    context: Context[F]
) extends View[F]:

  // TODO: Handle unsafe `get`s
  override def view(): F[Unit] =
    for
      rawResult <- showPrompt(context.prompt, buildActionsPrompt)
      actionId = getListPromptResult(rawResult.get(menuResultsName).get)
      action = actions.get(actionId).get
      _ <- action.onSelect()
    yield ()

  private val menuResultsName = "menu"

  private case class Action(text: String, onSelect: () => F[Unit])
  private val actions = Map(
    "crawl" -> Action(text = "Trigger a crawl", onSelect = triggerCrawl),
    "recent-releases" -> Action(
      text = "Browse recent releases",
      onSelect = browseRecentReleases
    ),
    "asset-management" -> Action(
      text = "Manage assets",
      onSelect = manageAssets
    ),
    "exit" -> Action(text = "exit", onSelect = () => Applicative[F].unit)
  )

  private def triggerCrawl(): F[Unit] =
    context.services.crawler
      .crawl()
      .run(context.services.crawlerLibrary) >> view()

  private def browseRecentReleases(): F[Unit] =
    for
      minDate <- retryUntilSuccess(
        getDateInput(s"Enter min. release date ($dateStringFormat):")
      )
      assets <- context.services.assets.findRecentReleases(
        DateReleased(minDate)
      )
      _ <- new CrawlResultsView[F](context, assets, this).view()
    yield ()

  private def manageAssets(): F[Unit] =
    new MainAssetManagementView(context, this).view()

  private def buildActionsPrompt =
    val promptBuilder = context.prompt.getPromptBuilder()

    val header = promptBuilder
      .createListPrompt()
      .name(menuResultsName)
      .message("Pick an action:")

    actions
      .foldLeft(header) { (builder, item) =>
        val id = item._1
        val action = item._2
        builder.newItem(id).text(action.text).add()
      }
      .addPrompt()
      .build()

  private val dateStringFormat = "yyyy-MM-dd"
  private val format = new SimpleDateFormat(dateStringFormat)
  private def getDateInput(message: String): F[Either[Throwable, Date]] =
    getInput(context.prompt, message).flatMap { input =>
      Try(format.parse(input)).toEither match
        case Left(reason) => Console[F].println(reason) *> reason.asLeft.pure
        case Right(value) => value.asRight[Throwable].pure
    }
