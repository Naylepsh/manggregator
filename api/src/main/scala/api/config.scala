package api

import com.comcast.ip4s.*

object config:
  case class ServerConfig(host: Host, port: Port)

  case class Docs(title: String, version: String)
