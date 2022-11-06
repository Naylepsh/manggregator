package library.domain

import java.util.UUID
import monix.newtypes._

object alias:
  import library.domain.asset.AssetId

  type AliasId = AliasId.Type
  object AliasId extends NewtypeWrapped[UUID]

  type AliasName = AliasName.Type
  object AliasName extends NewtypeWrapped[String]

  case class Alias(id: AliasId, assetId: AssetId, name: AliasName)

  case class CreateAlias(name: AliasName)
