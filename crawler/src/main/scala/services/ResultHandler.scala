package services

import cats._
import cats.implicits._
import cats.data._
import cats.effect._
import cats.effect.std._
import cats.effect.implicits._
import org.legogroup.woof.{given, *}
import scala.concurrent.duration._
import scala.language.postfixOps
import crawler.domain.Library
import crawler.domain.Crawl.CrawlResult._

trait ResultHandler[F[_]]:
  def handle(resultsToExpect: Int): F[Unit]

object ResultHandler:
  def make[F[_]: Async: Logger](
      queue: Queue[F, Result],
      library: Library[F]
  ): ResultHandler[F] = new ResultHandler[F]:
    override def handle(resultsToExpect: Int): F[Unit] =
      def keepGoing(resultsLeft: Int): F[Unit] =
        if (resultsLeft > 0)
          for {
            _ <- Logger[F].info(s"$resultsLeft results left to go...")
            potentialResult <- queue.tryTake
            _ <- potentialResult match {
              case Some(result) =>
                Logger[F].info(s"Picked up one of $resultsLeft results left.")
                  *> library.handleResult(result)
                  *> keepGoing(resultsLeft - 1)

              case None =>
                Logger[F].info(s"Waiting for $resultsLeft results. Zzz...")
                  *> Async[F].sleep(5 seconds)
                  *> keepGoing(resultsLeft)
            }
          } yield ()
        else Logger[F].info(s"Handled all $resultsToExpect results.")

      keepGoing(resultsToExpect)
