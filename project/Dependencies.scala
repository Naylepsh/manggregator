import sbt._

object Dependencies {
  object V {
    val catsEffect = "3.3.14"
    val catsRetry = "3.1.0"
    val scalaScraper = "3.0.0"
    val scalaTime = "2.32.0"
    val ciris = "3.0.0"
    val circe = "0.14.3"
    val sttp = "3.8.8"
    val http4s = "0.23.16"
    val tapir = "1.1.3"
    val slf4j = "1.7.36"
    val woof = "0.4.7"
    val doobie = "1.0.0-RC2"
    val sqliteJDB = "3.40.0.0"

    val munit = "0.7.29"
    val munitCatsEffect = "1.0.7"
    val weaver = "0.8.1"
    val consoleUi = "0.0.13"
    val tui = "0.0.5"
  }

  object Libraries {
    val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect
    val catsRetry = "com.github.cb372" %% "cats-retry" % V.catsRetry
    val scalaScraper = "net.ruippeixotog" %% "scala-scraper" % V.scalaScraper
    val scalaTime = "com.github.nscala-time" %% "nscala-time" % V.scalaTime
    val ciris = "is.cir" %% "ciris" % V.ciris
    val consoleUi = "de.codeshelf.consoleui" % "consoleui" % V.consoleUi
    val tui = "com.olvind.tui" %% "tui" % V.tui
    val crossterm = "com.olvind.tui" % "crossterm" % V.tui

    val circe = "io.circe" %% "circe-core" % V.circe
    val circeGeneric = "io.circe" %% "circe-generic" % V.circe
    val circeParser = "io.circe" %% "circe-parser" % V.circe

    val sttp = "com.softwaremill.sttp.client3" %% "core" % V.sttp
    val sttpCats = "com.softwaremill.sttp.client3" %% "cats" % V.sttp
    val sttpCirce = "com.softwaremill.sttp.client3" %% "circe" % V.sttp

    val woof = "org.legogroup" %% "woof-core" % V.woof
    val slf4j = "org.slf4j" % "slf4j-simple" % V.slf4j

    val http4sEmberServer =
      "org.http4s" %% "http4s-ember-server" % V.http4s
    val http4sEmberClient =
      "org.http4s" %% "http4s-ember-client" % V.http4s
    val http4sCirce = "org.http4s" %% "http4s-circe" % V.http4s
    val http4sDsl = "org.http4s" %% "http4s-dsl" % V.http4s

    val tapirHttp4s =
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % V.tapir
    val tapirSwagger =
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % V.tapir
    val tapirJsonCirce =
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % V.tapir

    val doobie = "org.tpolecat" %% "doobie-core" % V.doobie
    val doobieHikari = "org.tpolecat" %% "doobie-hikari" % V.doobie
    val sqliteJDB = "org.xerial" % "sqlite-jdbc" % V.sqliteJDB

    val munit = "org.scalameta" %% "munit" % V.munit
    val munitCatsEffect =
      "org.typelevel" %% "munit-cats-effect-3" % V.munitCatsEffect
    val weaver = "com.disneystreaming" %% "weaver-cats" % V.weaver
  }
}
