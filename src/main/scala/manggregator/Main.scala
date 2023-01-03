package manggregator

import cats._
import cats.effect._

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    args.head match
      case "server"       => apps.Server.run()
      case "single-crawl" => apps.SingleCrawl.run()
