package api.utils

import cats._
import cats.data._
import cats.syntax._
import cats.implicits._
import org.http4s.HttpRoutes
import scala.annotation.tailrec

object routes:
  given given_Semigroup[F[_]: Monad]: Semigroup[HttpRoutes[F]] with {
    def combine(x: HttpRoutes[F], y: HttpRoutes[F]): HttpRoutes[F] = x <+> y
  }
