package http

import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import library.domain.AssetRepository

object Server:

  def server(assetRepository: AssetRepository) =
    val router = Routes.router(assetRepository)
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(router)
      .build
      .use(_ => IO.never)
