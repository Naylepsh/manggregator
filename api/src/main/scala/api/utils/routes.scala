package api.utils

import scala.annotation.tailrec

import cats._
import cats.data._
import cats.implicits._
import cats.syntax._
import org.http4s.HttpRoutes

object routes:
  given given_Semigroup[F[_]: Monad]: Semigroup[HttpRoutes[F]] with {
    def combine(x: HttpRoutes[F], y: HttpRoutes[F]): HttpRoutes[F] = x <+> y
  }
