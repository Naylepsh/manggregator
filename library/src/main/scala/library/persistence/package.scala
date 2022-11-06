package library

package object persistence:
  case class Storage[F[_]](
      assets: Assets[F],
      chapters: Chapters[F],
      pages: Pages[F]
  )
