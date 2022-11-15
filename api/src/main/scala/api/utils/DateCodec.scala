package api.utils

import io.circe._
import scala.util.Try
import java.text.SimpleDateFormat
import java.util.Date
import cats._
import cats.implicits._

object DateCodec:
  val dateFormatter = SimpleDateFormat("dd MMM yyyy")

  given decodeDate: Decoder[Date] = Decoder.decodeString.emap { dateString =>
    Try(dateFormatter.parse(dateString)).toEither.leftMap(_.toString)
  }
  given encodeDate: Encoder[Date] = new Encoder[Date] {
    final def apply(date: Date): Json = Json.fromString(date.toGMTString())
  }
