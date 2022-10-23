package api.library

import api.domain.Api
import api.library.Routes
import api.library.Endpoints
import sttp.tapir.Endpoint
import org.http4s.HttpRoutes

object LibraryApi:
  def apply(props: Routes.Props): Api = new Api {
    val endpoints: List[Endpoint[Unit, ?, ?, ?, ?]] = Endpoints.endpoints
    val routes: HttpRoutes[cats.effect.IO] = Routes.routes(props)
  }
