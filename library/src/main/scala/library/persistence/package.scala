package library

import java.util.UUID

import doobie.*
import doobie.implicits.*
import doobie.util.query.*

package object persistence:
  case class Storage[F[_]](
      assets: Assets[F],
      chapters: Chapters[F],
      pages: Pages[F]
  )

  object mappings:
    given Get[UUID] = Get[String].map(UUID.fromString)
    given Put[UUID] = Put[String].contramap(_.toString)
