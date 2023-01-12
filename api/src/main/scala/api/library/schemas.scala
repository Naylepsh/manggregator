package api.library

import java.net.URI
import sttp.tapir._
import scala.util.Try

object schemas:
  given Schema[URI] =
    Schema.string.description("URL").encodedExample("https://example.com")
