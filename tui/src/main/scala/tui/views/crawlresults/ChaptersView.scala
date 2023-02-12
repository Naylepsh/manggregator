package tui.views.crawlresults

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Try

import cats.Applicative
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.implicits._
import de.codeshelf.consoleui.prompt.{CheckboxResult, PromtResultItemIF}
import library.domain.chapter.{Chapter, ChapterId}
import tui.prompts.menu
import tui.views.{Context, View}

class ChaptersView[F[_]: Sync: Console](
    context: Context[F],
    chapters: List[Chapter],
    previous: View[F]
) extends View[F]:

  override def view(): F[Unit] =
    for
      rawResult <- context.prompt.prompt(menu.build()).asScala.pure
      selectedIds = parsePromptResult(rawResult)
      _ <- selectedIds match
        case None      => Applicative[F].unit
        case Some(ids) => markChaptersAsSeen(ids)
      _ <- previous.view()
    yield ()

  private val promptName = "chapters-to-mark"

  private val menu =
    val core = context.prompt
      .getPromptBuilder()
      .createCheckboxPrompt()
      .name(promptName)
      .message("Select chapters to mark as seen")

    chapters
      .foldLeft(core) { case (builder, chapter) =>
        builder
          .newItem(chapter.id.value.toString)
          .text(s"${chapter.no.value} | ${chapter.url.value}")
          .add()
      }
      .addPrompt()

  private def parsePromptResult(
      result: mutable.Map[String, ? <: PromtResultItemIF]
  ): Option[List[String]] =
    result
      .get(promptName)
      .flatMap { value =>
        Try(value.asInstanceOf[CheckboxResult]).toOption
      }
      .map(_.getSelectedIds().asScala.toList)

  private def markChaptersAsSeen(ids: List[String]): F[Unit] =
    ids.traverse(ChapterId.apply) match
      case Left(reason) => Console[F].println(reason)
      case Right(ids)   => context.services.chapters.markAsSeen(ids)
