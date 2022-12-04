package library.config

import io.estatico.newtype.macros.newtype

object types:
  @newtype case class DatabasePath(value: String)
  @newtype case class DatabaseUsername(value: String)
  @newtype case class DatabasePassword(value: String)

  case class DatabaseConfig(
      path: DatabasePath,
      username: DatabaseUsername,
      password: DatabasePassword
  )
