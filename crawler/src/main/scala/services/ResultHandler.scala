package services

import scala.concurrent.duration._
import scala.language.postfixOps

import cats._
import cats.data._
import cats.effect._
import cats.effect.implicits._
import cats.effect.std._
import cats.implicits._
import crawler.domain.Crawl.CrawlResult._
import crawler.domain.Library
import org.legogroup.woof.{_, given}

trait ResultHandler[F[_]]:
  def handle(resultsToExpect: Int): F[Unit]

object ResultHandler:
  def make[F[_]: Async: Logger](
      queue: Queue[F, Result],
      library: Library[F]
  ): ResultHandler[F] = new ResultHandler[F]:
    override def handle(resultsToExpect: Int): F[Unit] =
      keepGoing(Metadata(resultsToExpect))

    private def keepGoing(metadata: Metadata): F[Unit] =
      if (metadata.resultsLeft > 0)
        for
          _ <- Logger[F].info(s"${metadata.resultsLeft} results left to go...")
          potentialResult <- queue.tryTake
          newMetadata <- potentialResult match
            case Some(result) =>
              handleResult(metadata, result).as(metadata.bumpFrom(result))

            case None => waitForResult(metadata).as(metadata)
          _ <- keepGoing(newMetadata)
        yield ()
      else
        Logger[F].info(
          s"""
          |Handled all ${metadata.resultsToExpect} results. 
          |Got ${metadata.successes} successful results 
          |and ${metadata.failures} failures""".stripMargin
        )

    private def handleResult(metadata: Metadata, result: Result): F[Unit] =
      Logger[F].info(
        s"Picked up one of ${metadata.resultsLeft} results left."
      )
        *> (result match
          case Left(error) =>
            Logger[F].error(
              s"A job for ${error.url.value} ended in failure due to ${error.reason}"
            )
          case Right(data) => library.handleResult(data)
        )

    private def waitForResult(
        metadata: Metadata
    ): F[Unit] =
      Logger[F].info(s"Waiting for ${metadata.resultsLeft} results. Zzz...")
        *> Async[F].sleep(5 seconds)

  private case class Metadata(
      resultsToExpect: Int,
      resultsLeft: Int,
      successes: Int,
      failures: Int
  ):
    def bumpFrom(result: Result): Metadata = result match
      case Left(_)  => addFailure
      case Right(_) => addSuccess

    def addSuccess: Metadata =
      this.copy(
        successes = successes + 1,
        resultsLeft = resultsLeft - 1
      )

    def addFailure: Metadata =
      this.copy(
        failures = failures + 1,
        resultsLeft = resultsLeft - 1
      )

  private object Metadata:
    def apply(resultsToExpect: Int): Metadata = Metadata(
      resultsToExpect,
      resultsToExpect,
      0,
      0
    )
