package library.domain

import java.util.UUID

import core.Newtype

object alias:
  import library.domain.asset.AssetId

  type AliasId = AliasId.Type
  object AliasId extends Newtype[UUID]

  type AliasName = AliasName.Type
  object AliasName extends Newtype[String]

  case class Alias(id: AliasId, assetId: AssetId, name: AliasName)

  case class CreateAlias(name: AliasName)
