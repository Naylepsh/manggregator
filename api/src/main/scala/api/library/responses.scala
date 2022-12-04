package api.library

import java.util.UUID

import io.circe._

object responses:
  case class CreateAssetResponse(assetId: UUID)

  case class CreateChaptersPageResponse(pageId: UUID)
