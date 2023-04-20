package crawler.services.httpclient

import cats.*
import cats.effect.kernel.Async
import cats.implicits.*
import retry.RetryPolicies.*
import retry.*
import sttp.capabilities.Effect
import sttp.client3.*

import concurrent.duration.DurationInt

class RetryingBackend[F[_]: Async, P](
    delegate: SttpBackend[F, P]
) extends DelegateSttpBackend[F, P](delegate):
  import RetryingBackend.*

  override def send[T, R >: P with Effect[F]](
      request: Request[T, R]
  ): F[Response[T]] =
    sendWithBackoff(request)

  private def sendWithBackoff[T, R >: P with Effect[F]](
      request: Request[T, R]
  ): F[Response[T]] =
    retryingOnAllErrors(
      policy = limitRetries[F](5) join exponentialBackoff[F](100.milliseconds),
      onError = doNothing
    )(delegate.send(request))

object RetryingBackend:
  def apply[F[_]: Async, P](
      delegate: SttpBackend[F, P]
  ): RetryingBackend[F, P] = new RetryingBackend(delegate)

  private def doNothing[F[_]: Applicative](
      err: Throwable,
      details: RetryDetails
  ): F[Unit] =
    Applicative[F].unit
