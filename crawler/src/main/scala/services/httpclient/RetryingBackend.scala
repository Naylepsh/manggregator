package crawler.services.httpclient

import concurrent.duration.DurationInt
import crawler.domain.Url
import retry.RetryPolicies._
import retry._
import cats._
import cats.implicits._
import cats.effect.kernel.Async
import sttp.capabilities.Effect
import sttp.client3._

class RetryingBackend[F[_]: Async, P](
    delegate: SttpBackend[F, P]
) extends DelegateSttpBackend[F, P](delegate):
  import RetryingBackend._

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
