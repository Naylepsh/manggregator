package api.utils

import scala.annotation.tailrec

import cats._
import cats.data._
import cats.implicits._
import cats.syntax._
import org.http4s.HttpRoutes

object routes:
  def combine[F[_]: Monad](
      routes: NonEmptyList[HttpRoutes[F]]
  ): HttpRoutes[F] =
    combine(routes.tail, routes.head)

  @tailrec
  private def combine[F[_]: Monad](
      routesLeft: List[HttpRoutes[F]],
      accRoute: HttpRoutes[F]
  ): HttpRoutes[F] = routesLeft match {
    case route :: next => combine(next, accRoute <+> route)
    case Nil           => accRoute
  }
