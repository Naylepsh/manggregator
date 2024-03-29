package api.library

import java.net.URI

import scala.util.Try

import core.Url
import io.circe.{ Decoder, Encoder }
import library.domain.alias.*
import library.domain.asset.*
import library.domain.chapter.*

object codecs:
  // --- Asset codecs ---
  given Encoder[AssetId]   = Encoder[String].contramap(_.value.toString)
  given Encoder[AssetName] = Encoder[String].contramap(_.value)
  given Encoder[Enabled]   = Encoder.encodeBoolean.contramap(_.value)

  // --- Alias codecs ---
  given Encoder[AliasId]   = Encoder[String].contramap(_.value.toString)
  given Encoder[AliasName] = Encoder[String].contramap(_.value)

  // --- Chapter codecs ---
  given Encoder[ChapterId]    = Encoder[String].contramap(_.value.toString)
  given Encoder[ChapterNo]    = Encoder[String].contramap(_.value)
  given Encoder[ChapterUrl]   = Encoder[String].contramap(_.value)
  given Encoder[DateReleased] = Encoder[String].contramap(_.value.toString)

  given Encoder[URI] = Encoder[String].contramap(_.toString)
  given Decoder[URI] = Decoder.decodeString.emap(Url.uri)
