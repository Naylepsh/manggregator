package api.domain

import java.util.UUID

object Response:
  case class CreatedResource(id: UUID)
