package api.library

import java.net.URI

import scala.util.Try

import sttp.tapir._

object schemas:
  given Schema[URI] =
    Schema.string.description("URL").encodedExample("https://example.com")
