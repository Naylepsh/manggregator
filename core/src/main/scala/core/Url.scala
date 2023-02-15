package core

import java.net.URI

import scala.util.Try

type Url = Url.Type
object Url extends Newtype[String]:
  def valid(a: String): Either[String, Type] =
    Either.cond(isUrl(a), Url(a), s"$a is not a valid url")

  def isUrl(s: String): Boolean = uri(s).isRight

  def uri(s: String): Either[String, URI] =
    Try {
      // Just constructing URI is not enough to validate.
      // Even `new URI("string")` passes.
      // toURL has to be called.
      val uri = new URI(s)
      uri.toURL()
      uri
    }.toEither.left.map(_ => s"$s is not a valid url")
