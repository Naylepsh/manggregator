package ui.views.common

import ui.core.View
import tui.crossterm.KeyCode
import ui.core.ViewResult
import tui._
import tui.widgets.ParagraphWidget
import tui.widgets.BlockWidget
import ui.core.Keep
import ui.core.Exit
import ui.components.KeybindsNav
import java.text.SimpleDateFormat
import java.util.Date
import scala.util.Try
import cats.implicits._
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._
import ui.core.ChangeTo

class DateInputView(next: Date => View) extends View:
  import DateInputView._

  var inputMode: InputMode = InputMode.Editing
  var inputText: String = ""
  var invalidInputReason: Option[String] = None

  override def render(frame: Frame): Unit =
    val chunks = Layout(
      direction = Direction.Vertical,
      margin = Margin(2),
      constraints = Array(Constraint.Length(3), Constraint.Length(1))
    ).split(frame.size)

    chunks.toList match
      case input :: nav :: Nil =>
        renderInput(frame, input)
        renderCursor(frame, input)
        renderKeybindsNav(frame, nav)
      case _ =>

  override def handleInput(key: KeyCode): ViewResult =
    inputMode match
      case InputMode.Normal =>
        key match
          case c: KeyCode.Char if c.c == 'e' =>
            inputMode = InputMode.Editing
            Keep
          case c: KeyCode.Char if c.c == 'q' => Exit
          case _                             => Keep

      case InputMode.Editing =>
        key match
          case _: KeyCode.Enter =>
            DateInput.parse(inputText) match
              case Left(value) =>
                this.invalidInputReason = Some(value)
                Keep
              case Right(value) => ChangeTo(next(value))
          case c: KeyCode.Char =>
            inputText = inputText + c.c()
            Keep
          case _: KeyCode.Backspace =>
            if (!inputText.isBlank())
              inputText = inputText.substring(0, inputText.length - 1)
            Keep
          case _: KeyCode.Esc =>
            inputMode = InputMode.Normal
            Keep
          case _ => Keep

  private def renderKeybindsNav(frame: Frame, area: Rect): Unit =
    val keybinds = inputMode match
      case InputMode.Normal  => normalModeKeybinds
      case InputMode.Editing => editingModeKeybinds

    keybinds.render(frame, area)

  private def renderInput(frame: Frame, area: Rect): Unit =
    val input = ParagraphWidget(
      text = Text.nostyle(inputText),
      block = Some(
        BlockWidget(borders = Borders.ALL, title = Some(inputTitle))
      ),
      style = inputMode match {
        case InputMode.Normal  => Style.DEFAULT
        case InputMode.Editing => Style.DEFAULT.fg(Color.Yellow)
      }
    )

    frame.render_widget(input, area)

  private def inputTitle: Spans =
    val core = Span.nostyle(
      s"Min. release date (${DateInput.dateStringFormat} or [0-9]+d)"
    )

    invalidInputReason match
      case None =>
        Spans.from(core)
      case Some(reason) =>
        Spans.from(core, Span.styled(s" $reason", Style(fg = Some(Color.Red))))

  private def renderCursor(frame: Frame, area: Rect): Unit =
    inputMode match
      case InputMode.Normal =>
        // Hide the cursor. `Frame` does this by default, so we don't need to do anything here
        ()

      case InputMode.Editing =>
        // Make the cursor visible and ask tui-rs to put it at the specified coordinates after rendering
        frame.set_cursor(
          // Put cursor past the end of the input text
          x = area.x + Grapheme(inputText).width + 1,
          // Move one line down, from the border to the input line
          y = area.y + 1
        )

object DateInputView:
  sealed trait InputMode
  object InputMode:
    case object Normal extends InputMode
    case object Editing extends InputMode

  val normalModeKeybinds = KeybindsNav(List("e start editing", "q quit"))
  val editingModeKeybinds = KeybindsNav(
    List("Esc stop editing", "Enter submit")
  )

  object DateInput:
    private val absoluteDatePattern = "([0-9]{4})-([0-9]{2})-([0-9]{2})".r
    private val relativeDatePattern = "([0-9]+)d".r
    val dateStringFormat = "yyyy-MM-dd"
    private val format = new SimpleDateFormat(dateStringFormat)

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
