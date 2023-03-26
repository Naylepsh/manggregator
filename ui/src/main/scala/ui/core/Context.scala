package ui.core

import cats.effect.std.Dispatcher
import crawler.domain.Library
import crawler.services.Crawler
import library.services.{Assets, Chapters, Pages}

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
