package manggregator

import cats.*
import cats.effect.*

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    args.head match
      case "server" => apps.Server.run()
      case "ui"     => apps.UI.run()
