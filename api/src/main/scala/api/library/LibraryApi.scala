package api.library

import api.domain.Api
import api.library.endpoints.all as allEndpoints
import api.library.routes.{ Services, all as allRoutes }
import cats.effect.kernel.Async
import org.http4s.HttpRoutes
import sttp.tapir.Endpoint

object LibraryApi:
  def apply[F[_]: Async](props: Services[F]): Api[F] = new Api:
    val endpoints: List[Endpoint[Unit, ?, ?, ?, ?]] = allEndpoints
    val routes: HttpRoutes[F]                       = allRoutes(props)
