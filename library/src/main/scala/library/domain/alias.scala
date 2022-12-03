package library.domain

import java.util.UUID

import io.estatico.newtype.macros.newtype

object alias:
  import library.domain.asset.AssetId

  @newtype
  case class AliasId(value: UUID)

  @newtype
  case class AliasName(value: String)

  case class Alias(id: AliasId, assetId: AssetId, name: AliasName)

  case class CreateAlias(name: AliasName)
