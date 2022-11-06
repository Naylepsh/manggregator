package api.library

import io.circe._
import sttp.tapir.generic.auto._
import library.domain.asset._
import library.domain.alias._
import library.domain.chapter._
import api.utils.DateCodec.{encodeDate, decodeDate}
import java.util.UUID
import sttp.tapir.Schema
import sttp.tapir.codec.monix.newtype._
import scala.util.Try
import monix.newtypes.NewtypeWrapped

object codecs:
  /** Module for opaque type / newtype codecs
    */

  given Schema[Asset] = Schema.derived
  given Schema[Chapter] = Schema.derived

  // --- Asset codecs ---

  given Encoder[AssetId] = Encoder.encodeString.contramap(_.value.toString)
  given Decoder[AssetId] = decodeId(AssetId.apply)
  given Encoder[AssetName] = encodeString
  given Decoder[AssetName] = decodeString(AssetName.apply)
  given Encoder[Enabled] = Encoder.encodeBoolean.contramap(_.value)
  given Decoder[Enabled] = Decoder.decodeBoolean.map(Enabled.apply)

  // --- Alias codecs ---

  given Encoder[AliasId] = Encoder.encodeString.contramap(_.value.toString)
  given Decoder[AliasId] = decodeId(AliasId.apply)
  given Encoder[AliasName] = encodeString
  given Decoder[AliasName] = decodeString(AliasName.apply)

  // --- Chapter codecs ---

  given Encoder[ChapterId] = Encoder.encodeString.contramap(_.value.toString)
  given Decoder[ChapterId] = decodeId(ChapterId.apply)
  given Encoder[ChapterNo] = encodeString
  given Decoder[ChapterNo] = decodeString(ChapterNo.apply)
  given Encoder[ChapterUrl] = encodeString
  given Decoder[ChapterUrl] = decodeString(ChapterUrl.apply)
  given Encoder[DateReleased] = encodeDate.contramap(_.value)
  given Decoder[DateReleased] = decodeDate.map(DateReleased.apply)

  // --- Helpers ---

  def decodeId[F](pure: UUID => F): Decoder[F] =
    Decoder.decodeString.emapTry(str => Try(UUID.fromString(str)).map(pure))

  def encodeString[F]: Encoder[F] =
    Encoder.encodeString.contramap[F](_.toString)
  def decodeString[F](pure: String => F): Decoder[F] =
    Decoder.decodeString.map(pure)
