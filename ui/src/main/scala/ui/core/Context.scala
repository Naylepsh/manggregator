package ui.core

import crawler.domain.Library
import crawler.services.Crawler
import library.services.{Assets, Chapters, Pages}
import cats.effect.std.Dispatcher

case class Context[F[_]](
    theme: Theme,
    services: Services[F],
    dispatcher: Dispatcher[F]
)

case class Services[F[_]](
    assets: Assets[F],
    pages: Pages[F],
    chapters: Chapters[F],
    crawler: Crawler[F],
    crawlerLibrary: Library[F]
)
