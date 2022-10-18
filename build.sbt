ThisBuild / scalaVersion := "3.2.0"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / name := "manggregator"
ThisBuild / organization := "io.naylepsh"

lazy val root = project
  .in(file("."))
  .settings(libraryDependencies ++= commonDependencies)
  .aggregate(crawler, library)
  .dependsOn(crawler, library)

lazy val crawler = project.settings(
  name := "crawler",
  libraryDependencies ++= commonDependencies ++ Seq(
    dependencies.scalaScraper,
    dependencies.scalaTime
  )
)

lazy val library = project.settings(
  name := "library",
  libraryDependencies ++= commonDependencies
)

val Http4sVersion = "0.23.16"
val TapirVersion = "1.1.3"

lazy val dependencies =
  new {
    val catsEffect = "org.typelevel" %% "cats-effect" % "3.3.14"
    val scalaScraper = "net.ruippeixotog" %% "scala-scraper" % "3.0.0"
    val scalaTime = "com.github.nscala-time" %% "nscala-time" % "2.32.0"
    val munit = "org.scalameta" %% "munit" % "0.7.29" % Test
    val munitCatsEffect =
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test
    val http4sEmberServer =
      "org.http4s" %% "http4s-ember-server" % Http4sVersion
    val http4sEmberClient =
      "org.http4s" %% "http4s-ember-client" % Http4sVersion
    val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion
    val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion
    val tapirHttp4s =
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % TapirVersion
    // val tapirSwagger =
    // "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui" % TapirVersion
    val tapirSwagger =
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % TapirVersion
    val slf4j = "org.slf4j" % "slf4j-simple" % "1.7.36"
  }

lazy val commonDependencies = Seq(
  dependencies.catsEffect,
  dependencies.http4sEmberClient,
  dependencies.http4sEmberServer,
  dependencies.http4sCirce,
  dependencies.http4sDsl,
  dependencies.tapirHttp4s,
  dependencies.tapirSwagger,
  dependencies.slf4j,
  dependencies.munit,
  dependencies.munitCatsEffect
)

addCommandAlias(
  "codeCoverage",
  "coverage ; test ; coverageReport"
)
