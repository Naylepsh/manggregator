package tui.views

import library.services.{Assets, Pages}
import de.codeshelf.consoleui.prompt.ConsolePrompt
import crawler.domain.Library
import crawler.services.Crawler

case class Services[F[_]](
    assets: Assets[F],
    pages: Pages[F],
    crawler: Crawler[F],
    crawlerLibrary: Library[F]
)

case class Context[F[_]](
    prompt: ConsolePrompt,
    services: Services[F]
)
