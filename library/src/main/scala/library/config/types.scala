package library.config

import core.Newtype

object types:
  type DatabasePath = DatabasePath.Type
  object DatabasePath extends Newtype[String]

  type DatabaseUsername = DatabaseUsername.Type
  object DatabaseUsername extends Newtype[String]

  type DatabasePassword = DatabasePassword.Type
  object DatabasePassword extends Newtype[String]

  case class DatabaseConfig(
      path: DatabasePath,
      username: DatabaseUsername,
      password: DatabasePassword
  )
