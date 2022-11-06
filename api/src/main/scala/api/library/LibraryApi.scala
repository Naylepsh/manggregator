package api.library

import api.domain.Api
import api.library.Routes
import api.library.Endpoints
import sttp.tapir.Endpoint
import org.http4s.HttpRoutes
import cats.effect.kernel.Async

object LibraryApi:
  def apply[F[_]: Async](props: Routes.Props[F]): Api[F] = new Api {
    val endpoints: List[Endpoint[Unit, ?, ?, ?, ?]] = Endpoints.endpoints
    val routes: HttpRoutes[F] = Routes.routes(props)
  }
