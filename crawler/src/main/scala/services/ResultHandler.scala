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
          _ <- potentialResult match {
            case Some(result) => handleResult(metadata, result)
            case None         => waitForResult(metadata)
          }
        yield ()
      else
        Logger[F].info(
          s"""
          |Handled all ${metadata.resultsToExpect} results. 
          |Got ${metadata.successes} successful results 
          |and ${metadata.failures} failures""".stripMargin
        )

    private def handleResult(metadata: Metadata, result: Result): F[Unit] =
      for
        _ <- Logger[F].info(
          s"Picked up one of ${metadata.resultsLeft} results left."
        )
        _ <- result match
          case Left(error) =>
            Logger[F].error(
              s"A job for ${error.url.value} ended in failure due to ${error.reason}"
            )
          case Right(data) => library.handleResult(data)
        newMetadata = result.fold(
          _ => Metadata.oneMoreFailure(metadata),
          _ => Metadata.oneMoreSuccess(metadata)
        )
        _ <- keepGoing(newMetadata)
      yield ()

    private def waitForResult(
        metadata: Metadata
    ): F[Unit] =
      Logger[F].info(s"Waiting for ${metadata.resultsLeft} results. Zzz...")
        *> Async[F].sleep(5 seconds)
        *> keepGoing(metadata)

  private case class Metadata(
      resultsToExpect: Int,
      resultsLeft: Int,
      successes: Int,
      failures: Int
  )
  private object Metadata:
    def apply(resultsToExpect: Int): Metadata = Metadata(
      resultsToExpect,
      resultsToExpect,
      0,
      0
    )

    def oneMoreSuccess(metadata: Metadata): Metadata =
      metadata.copy(
        successes = metadata.successes + 1,
        resultsLeft = metadata.resultsLeft - 1
      )

    def oneMoreFailure(metadata: Metadata): Metadata =
      metadata.copy(
        failures = metadata.failures + 1,
        resultsLeft = metadata.resultsLeft - 1
      )
