package ui.views.common

import java.text.SimpleDateFormat
import java.util.Date

import scala.util.Try

import cats.implicits.*
import com.github.nscala_time.time.Imports.*
import org.joda.time.DateTime
import ui.core.View

class DateInputView(next: Date => View, previousView: Option[View])
    extends InputView(
      DateInputView.DateInput.parse,
      s"Min. release date (${DateInputView.DateInput.dateStringFormat} or [0-9]+d)",
      next,
      previousView
    )

object DateInputView:
  object DateInput:
    private val absoluteDatePattern = "([0-9]{4})-([0-9]{2})-([0-9]{2})".r
    private val relativeDatePattern = "([0-9]+)d".r
    val dateStringFormat            = "yyyy-MM-dd"
    private val format              = new SimpleDateFormat(dateStringFormat)

    def parse(input: String): Either[String, Date] =
      input match
        case relativeDatePattern(days) =>
          days.toIntOption.fold(
            s"$days is not a valid int".asLeft
          )(days => (DateTime.now() - days.days).date.asRight)

        case absoluteDatePattern(_, _, _) =>
          Try(format.parse(input)).toEither.left.map(_.toString)

        case _ =>
          s"'$input' did not match any date formats".asLeft
