package tui.views

import java.text.SimpleDateFormat
import java.util.Date

import scala.jdk.CollectionConverters.*
import scala.util.Try

import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._
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
import tui.views.crawlresults.CrawlResultsView

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
        getDateInput(s"Enter min. release date ($dateStringFormat or [0-9]+d):")
      )
      assets <- context.services.assets.findRecentReleases(
        DateReleased(minDate)
      )
      _ <- new CrawlResultsView(context, assets, this).view()
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

  private def getDateInput(message: String): F[Either[Throwable, Date]] =
    getInput(context.prompt, message)
      .map(parseDateInput)
      .flatTap(showIfFailed)

  private val absoluteDatePattern = "([0-9]{4})-([0-9]{2})-([0-9]{2})".r
  private val relativeDatePattern = "([0-9]+)d".r
  private val dateStringFormat = "yyyy-MM-dd"
  private val format = new SimpleDateFormat(dateStringFormat)
  private def parseDateInput(input: String): Either[Throwable, Date] =
    input match
      case relativeDatePattern(days) =>
        parseRelativeDate(days)

      case absoluteDatePattern(_, _, _) =>
        Try(format.parse(input)).toEither

      case _ =>
        RuntimeException(s"$input did not match any date formats").asLeft

  private def parseRelativeDate(days: String): Either[Throwable, Date] =
    days.toIntOption.fold(
      RuntimeException(s"$days is not a valid int").asLeft
    )(days => (DateTime.now() - days.days).date.asRight)

  private def showIfFailed[A](a: Either[Throwable, A]): F[Unit] =
    a.fold(
      reason => Console[F].println(a.toString),
      _ => Applicative[F].unit
    )
