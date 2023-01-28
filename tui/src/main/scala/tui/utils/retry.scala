package tui.utils

import cats._
import cats.implicits._

object retry:
  def retryUntilSuccess[F[_]: Monad, A, E](
      action: => F[Either[E, A]]
  ): F[A] =
    action.flatMap(_ match
      case Left(_)      => retryUntilSuccess(action)
      case Right(value) => value.pure
    )
