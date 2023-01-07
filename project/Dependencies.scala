import sbt._

object Dependencies {
  object V {
    val catsEffect = "3.3.14"
    val scalaScraper = "3.0.0"
    val scalaTime = "2.32.0"
    val newType = "0.4.4"
    val ciris = "3.0.0"
    val http4s = "0.23.16"
    val tapir = "1.1.3"
    val slf4j = "1.7.36"
    val woof = "0.4.7"
    val doobie = "1.0.0-RC2"
    val sqliteJDB = "3.40.0.0"

    val munit = "0.7.29"
    val munitCatsEffect = "1.0.7"
    val weaver = "0.8.1"
  }

  object Libraries {
    val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect
    val scalaScraper = "net.ruippeixotog" %% "scala-scraper" % V.scalaScraper
    val scalaTime = "com.github.nscala-time" %% "nscala-time" % V.scalaTime
    val woof = "org.legogroup" %% "woof-core" % V.woof
    val newType = ("io.estatico" %% "newtype" % V.newType)
      .cross(CrossVersion.for3Use2_13)

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

    val ciris = "is.cir" %% "ciris" % V.ciris
    val slf4j = "org.slf4j" % "slf4j-simple" % V.slf4j
    val doobie = "org.tpolecat" %% "doobie-core" % V.doobie
    val doobieHikari = "org.tpolecat" %% "doobie-hikari" % V.doobie
    val sqliteJDB = "org.xerial" % "sqlite-jdbc" % V.sqliteJDB
    val munit = "org.scalameta" %% "munit" % V.munit % Test
    val munitCatsEffect =
      "org.typelevel" %% "munit-cats-effect-3" % V.munitCatsEffect % Test
    val weaver = "com.disneystreaming" %% "weaver-cats" % V.weaver
  }
}
