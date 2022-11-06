package api.domain

import org.http4s.HttpRoutes
import cats.effect._
import sttp.tapir._

trait Api[F[_]]:
  val endpoints: List[PublicEndpoint[_, _, _, _]]
  val routes: HttpRoutes[F]
