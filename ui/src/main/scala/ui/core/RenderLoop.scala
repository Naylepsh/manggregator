package ui.core

import cats.effect.kernel.Sync
import cats.implicits._
import tui._
import tui.crossterm.CrosstermJni
import java.time.{Duration, Instant}
import scala.math.Ordering.Implicits._
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import tui.crossterm.Event
import scala.util.control.Breaks._

class RenderLoop(initialView: View)(using IORuntime):
  val tickRate = Duration.ofMillis(250)

  def run(): IO[Unit] = withTerminal { (jni, terminal) =>
    runApp(terminal, jni, Instant.now, initialView).pure
  }

  private def runApp(
      terminal: Terminal,
      jni: CrosstermJni,
      lastTick: Instant,
      view: View
  ): Unit =
    // Rendering doesn't play nicely with IO monad,
    // hence the imperative code on the UI side
    var tick = lastTick
    var currentView = view

    breakable {
      while (true) {
        terminal.draw(f => currentView.render(f))
        val (viewAction, newTick) = handleInput(jni, tick, currentView)

        tick = newTick
        viewAction match
          case Exit => break
          case ChangeTo(view) =>
            currentView = view
          case Keep =>
      }
    }

  private def handleInput(
      jni: CrosstermJni,
      lastTick: Instant,
      view: View
  ): (ViewResult, Instant) =
    val duration = timeout(lastTick)
    val event = waitForInput(jni, duration)
    val viewAction = event.fold(Keep) {
      _ match
        case key: tui.crossterm.Event.Key =>
          view.handleInput(key.keyEvent.code)
        case _ => Keep
    }
    val newTick =
      if (elapsedSince(lastTick) >= tickRate) Instant.now else lastTick
    (viewAction, newTick)

  private def waitForInput(
      jni: CrosstermJni,
      duration: tui.crossterm.Duration
  ): Option[Event] =
    if (jni.poll(duration))
      jni.read().some
    else
      None

  private def elapsedSince(lastTick: Instant): Duration =
    java.time.Duration.between(lastTick, java.time.Instant.now())

  private def timeout(lastTick: Instant) =
    val diff = elapsedSince(lastTick)
    val timeout = tickRate.minus(diff)
    new tui.crossterm.Duration(timeout.toSeconds, timeout.getNano)
