package tui.views

import cats.effect.std.Console
import cats.effect.kernel.Sync
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt
import scala.jdk.CollectionConverters.*
import cats.Applicative
import java.util.Date
import java.text.SimpleDateFormat
import scala.util.Try
import tui.utils.retry.retryUntilSuccess
import library.services.Assets
import library.domain.chapter.DateReleased
import crawler.services.Crawler
import crawler.domain.Library

class MainMenuView[F[_]: Console: Sync](
    prompt: ConsolePrompt,
    assetService: Assets[F],
    crawlingService: Crawler[F],
    crawlingLibrary: Library[F]
) extends View[F]:

  // TODO: Handle unsafe `get`s
  override def view(): F[Unit] =
    for
      rawResult <- showPrompt(prompt, buildActionsPrompt)
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
    "exit" -> Action(text = "exit", onSelect = () => Applicative[F].unit)
  )

  private def triggerCrawl(): F[Unit] =
    crawlingService.crawl().run(crawlingLibrary) >> view()

  private def browseRecentReleases(): F[Unit] =
    for
      minDate <- retryUntilSuccess(
        getDateInput(s"Enter min. release date ($dateStringFormat):")
      )
      assets <- assetService.findRecentReleases(DateReleased(minDate))
      _ <- new CrawlResultsView[F](prompt, assets, this).view()
    yield ()

  private def buildActionsPrompt =
    val promptBuilder = prompt.getPromptBuilder()

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
    getInput(message).flatMap { input =>
      Try(format.parse(input)).toEither match
        case Left(reason) => Console[F].println(reason) *> reason.asLeft.pure
        case Right(value) => value.asRight[Throwable].pure
    }

  private def getInput(message: String): F[String] =
    val promptBuilder = prompt.getPromptBuilder()
    val promptId = "input"
    val inputPrompt = promptBuilder
      .createInputPrompt()
      .name(promptId)
      .message(message)
      .addPrompt()
      .build()

    Sync[F].pure(prompt.prompt(inputPrompt)).map { rawResult =>
      getInputPromptResult(rawResult.get(promptId))
    }
