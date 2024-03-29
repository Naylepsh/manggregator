package library.persistence

import java.util.UUID

import scala.collection.mutable.ListBuffer

import cats.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.effect.std.UUIDGen
import cats.effect.std.UUIDGen.randomUUID
import cats.implicits.*
import cats.syntax.*
import doobie.*
import doobie.implicits.*
import doobie.util.query.*
import library.domain.asset.AssetId
import library.domain.page.*

trait Pages[F[_]]:
  def create(page: CreateChaptersPage): F[PageId]
  // def createMany(pages: List[AssetPage]): IO[Unit]

  def findByUrl(url: PageUrl): F[Option[ChaptersPage]]
  def findByAssetIds(assetIds: List[AssetId]): F[List[ChaptersPage]]

object Pages:
  import PagesSQL.*

  def makeSQL[F[_]: MonadCancelThrow: UUIDGen](
      xa: Transactor[F]
  ): Pages[F] = new Pages[F]:

    override def create(page: CreateChaptersPage): F[PageId] =
      for
        id <- randomUUID
        _ <- insert(
          ChaptersPageRecord(
            id = id,
            site = page.site.value,
            url = page.url.value,
            assetId = page.assetId.value
          )
        ).run.transact(xa)
      yield PageId(id)

    override def findByUrl(url: PageUrl): F[Option[ChaptersPage]] =
      selectByUrl(url.value).option
        .map(_.map(ChaptersPageRecord.toDomain))
        .transact(xa)

    override def findByAssetIds(
        assetIds: List[AssetId]
    ): F[List[ChaptersPage]] =
      NonEmptyList.fromList(assetIds).fold(List.empty.pure) { ids =>
        selectByAssetIds(ids.map(_.value))
          .to[List]
          .map(_.map(ChaptersPageRecord.toDomain))
          .transact(xa)
      }

object PagesSQL:
  import mappings.given

  case class ChaptersPageRecord(
      id: UUID,
      assetId: UUID,
      site: String,
      url: String
  )
  object ChaptersPageRecord:
    def toDomain(record: ChaptersPageRecord): ChaptersPage =
      ChaptersPage(
        id = PageId(record.id),
        site = Site(record.site),
        url = PageUrl(record.url),
        assetId = AssetId(record.assetId)
      )

  def insert(record: ChaptersPageRecord): Update0 =
    sql"""
      INSERT INTO chapters_page (id, site, url, assetId)
      VALUES (${record.id}, ${record.site}, ${record.url}, ${record.assetId})
    """.update

  def selectByUrl(url: String): Query0[ChaptersPageRecord] =
    sql"""
    SELECT * from chapters_page
    WHERE url = $url
    """.query[ChaptersPageRecord]

  def selectByAssetIds(
      assetIds: NonEmptyList[UUID]
  ): Query0[ChaptersPageRecord] =
    (
      sql"""
      SELECT * from chapters_page
      WHERE """ ++ Fragments.in(fr"assetId", assetIds)
    ).query[ChaptersPageRecord]
