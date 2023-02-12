package tui.resources

import cats.effect.kernel.{Resource, Sync}
import cats.effect.std.Console
import cats.implicits._
import crawler.domain.Library
import crawler.services.Crawler
import de.codeshelf.consoleui.prompt.ConsolePrompt
import library.services.{Assets, Chapters, Pages}
import org.fusesource.jansi.AnsiConsole
import tui.views._

trait MakeTUI[F[_]]:
  def make(): Resource[F, View[F]]

object MakeTUI:
  def apply[F[_]: Sync: Console](
      assets: Assets[F],
      pages: Pages[F],
      chapters: Chapters[F],
      crawler: Crawler[F],
      crawlerLibrary: Library[F]
  ): MakeTUI[F] =
    new MakeTUI[F]:
      override def make(): Resource[F, View[F]] =
        Resource.make(
          registerConsole().map(_ =>
            new MainMenuView[F](
              Context(
                new ConsolePrompt(),
                Services(
                  assets,
                  pages,
                  chapters,
                  crawler,
                  crawlerLibrary
                )
              )
            )
          )
        )(_ => unregisterConsole())

      private def registerConsole[F[_]: Sync](): F[Unit] = Sync[F].pure {
        AnsiConsole.systemInstall()
      }

      private def unregisterConsole[F[_]: Sync](): F[Unit] = Sync[F].pure {
        AnsiConsole.systemUninstall()
      }
