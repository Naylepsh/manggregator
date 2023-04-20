package manggregator.entrypoints

import java.io.{ FileWriter, PrintWriter }

import cats.effect.IO
import cats.effect.kernel.Resource
import org.legogroup.woof.{ *, given }

object Logging:
  def console(): Output[IO] =
    Output.fromConsole

  def noop(): Output[IO] =
    new Output[IO]:
      override def output(str: String): IO[Unit]      = IO.unit
      override def outputError(str: String): IO[Unit] = IO.unit

  def file(): Output[IO] =
    def writeLine(line: String, logPath: String) =
      val writer = IO(PrintWriter(FileWriter(logPath, true)))
      val res    = Resource.make(writer)(w => IO(w.close))
      res.use(w => IO(w.println(line)))

    new Output[IO]:
      def output(str: String)      = writeLine(str, "woof.log")
      def outputError(str: String) = writeLine(str, "woof.err")
