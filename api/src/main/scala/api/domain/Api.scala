package api.domain

import cats.effect.*
import org.http4s.HttpRoutes
import sttp.tapir.*

trait Api[F[_]]:
  val endpoints: List[PublicEndpoint[?, ?, ?, ?]]
  val routes: HttpRoutes[F]
