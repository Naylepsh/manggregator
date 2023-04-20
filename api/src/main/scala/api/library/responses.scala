package api.library

import java.util.{ Date, UUID }

import io.circe.*
import library.domain.asset.AssetSummary

object responses:
  case class CreateAssetResponse(assetId: UUID)

  case class CreateChaptersPageResponse(pageId: UUID)

  case class AssetSummaryChapter(
      id: UUID,
      no: String,
      url: String,
      dateReleased: Date
  )
  case class AssetSummaryResponse(
      id: UUID,
      name: String,
      enabled: Boolean,
      chapters: List[AssetSummaryChapter]
  )
  object AssetSummaryResponse:
    def apply(summary: AssetSummary): AssetSummaryResponse =
      AssetSummaryResponse(
        id = summary.asset.id.value,
        name = summary.asset.name.value,
        enabled = summary.asset.enabled.value,
        chapters = summary.chapters.map(chapter =>
          AssetSummaryChapter(
            id = chapter.id.value,
            no = chapter.no.value,
            url = chapter.url.value,
            dateReleased = chapter.dateReleased.value
          )
        )
      )
