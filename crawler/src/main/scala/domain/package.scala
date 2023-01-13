package crawler

import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops._
import scala.util.Try
import java.net.URI

package object domain:
  @newtype
  case class Url(value: String)
  object Url:
    def fromString(s: String): Either[String, Url] =
      Either.cond(isUrl(s), Url(s), s"$s is not a valid url")

  private def isUrl(s: String): Boolean = {
    Try {
      // Just constructing URI is not enough to validate.
      // Even `new URI("string")` passes.
      // toURL has to be called.
      new URI(s).toURL()
    }.isSuccess
  }
