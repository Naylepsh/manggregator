package crawler.resources

import cats._
import cats.effect.kernel.{Async, Resource}
import cats.implicits._
import cats.syntax.all._
import sttp.capabilities.WebSockets
import sttp.client3.SttpBackend
import sttp.client3.httpclient.cats.HttpClientCatsBackend

object httpclient:
  type HttpClient[F[_]] = Resource[F, SttpBackend[F, WebSockets]]
  def makeClient[F[_]: Async: Functor]: HttpClient[F] =
    /** STTP docs recommend closing the backend on application exit. This should
      * work the same as normal client backend, except it should do the closing
      * automatically
      */
    HttpClientCatsBackend.resource[F]().flatMap { backend =>
      Resource.make(backend.pure)(_.close())
    }
