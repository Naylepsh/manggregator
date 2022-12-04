package api.library

import io.circe.Encoder
import library.domain.alias._
import library.domain.asset._
import library.domain.chapter._

object codecs:
  // --- Asset codecs ---
  given Encoder[AssetId] = Encoder[String].contramap(_.value.toString)
  given Encoder[AssetName] = Encoder[String].contramap(_.value)
  given Encoder[Enabled] = Encoder.encodeBoolean.contramap(_.value)

  // --- Alias codecs ---
  given Encoder[AliasId] = Encoder[String].contramap(_.value.toString)
  given Encoder[AliasName] = Encoder[String].contramap(_.value)

  // --- Chapter codecs ---
  given Encoder[ChapterId] = Encoder[String].contramap(_.value.toString)
  given Encoder[ChapterNo] = Encoder[String].contramap(_.value)
  given Encoder[ChapterUrl] = Encoder[String].contramap(_.value)
  given Encoder[DateReleased] = Encoder[String].contramap(_.value.toString)
