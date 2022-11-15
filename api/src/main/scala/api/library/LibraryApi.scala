package api.library

import api.domain.Api
import api.library.routes.{Services, all as allRoutes}
import api.library.endpoints.all as allEndpoints
import sttp.tapir.Endpoint
import org.http4s.HttpRoutes
import cats.effect.kernel.Async

object LibraryApi:
  def apply[F[_]: Async](props: Services[F]): Api[F] = new Api {
    val endpoints: List[Endpoint[Unit, ?, ?, ?, ?]] = allEndpoints
    val routes: HttpRoutes[F] = allRoutes(props)
  }
