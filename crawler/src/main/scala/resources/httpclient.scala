package crawler.resources

import sttp.client3.httpclient.cats.HttpClientCatsBackend
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats._
import cats.implicits._
import cats.syntax.all._
import sttp.client3.SttpBackend
import sttp.capabilities.WebSockets

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
