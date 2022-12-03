package library

import java.util.UUID

import doobie._
import doobie.implicits._
import doobie.util.query._

package object persistence:
  case class Storage[F[_]](
      assets: Assets[F],
      chapters: Chapters[F],
      pages: Pages[F]
  )

  object mappings:
    given Get[UUID] = Get[String].map(UUID.fromString)
    given Put[UUID] = Put[String].contramap(_.toString)
