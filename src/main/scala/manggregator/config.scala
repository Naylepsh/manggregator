package manggregator

import api.config.{ServerConfig, _}
import cats._
import cats.effect._
import cats.implicits._
import ciris._
import com.comcast.ip4s._
import library.config.types._

object config:
  case class ApplicationConfig(
      database: DatabaseConfig,
      server: ServerConfig,
      apiDocs: Docs
  )

  def load[F[_]: Async] = databaseConfig.load[F].map { database =>
    ApplicationConfig(database, serverConfig, apiDocsConfig)
  }

  private val databaseConfig: ConfigValue[Effect, DatabaseConfig] =
    (
      env("DATABASE_PATH").map(value => DatabasePath(value)),
      env("DATABASE_USERNAME").map(value => DatabaseUsername(value)),
      env("DATABASE_PASSWORD").map(value => DatabasePassword(value))
    ).parMapN(DatabaseConfig.apply)

  private val serverConfig = ServerConfig(
    ipv4"0.0.0.0",
    port"8080"
  )

  private val apiDocsConfig = Docs(title = "MANGgregator", version = "0.0.1")
