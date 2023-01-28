package tui.resources

import cats.effect.kernel.Resource
import tui.views.View
import library.services.Assets
import cats.effect.kernel.Sync
import cats.effect.std.Console
import org.fusesource.jansi.AnsiConsole
import tui.views.MainMenuView
import cats.implicits._
import de.codeshelf.consoleui.prompt.ConsolePrompt

trait MakeTUI[F[_]]:
  def make(): Resource[F, View[F]]

object MakeTUI:
  def apply[F[_]: Sync: Console](assets: Assets[F]): MakeTUI[F] =
    new MakeTUI[F]:
      override def make(): Resource[F, View[F]] =
        Resource.make(
          registerConsole().map(_ =>
            new MainMenuView[F](new ConsolePrompt(), assets)
          )
        )(_ => unregisterConsole())

      private def registerConsole[F[_]: Sync](): F[Unit] = Sync[F].pure {
        AnsiConsole.systemInstall()
      }

      private def unregisterConsole[F[_]: Sync](): F[Unit] = Sync[F].pure {
        AnsiConsole.systemUninstall()
      }
