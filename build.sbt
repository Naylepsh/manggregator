ThisBuild / scalaVersion := "3.2.0"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / name := "manggregator"
ThisBuild / organization := "io.naylepsh"
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "manggregator",
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.ciris,
      dependencies.slf4j
    )
  )
  .aggregate(crawler, library, api)
  .dependsOn(crawler, library, api)

lazy val crawler = project.settings(
  name := "crawler",
  libraryDependencies ++= commonDependencies ++ Seq(
    dependencies.scalaScraper,
    dependencies.scalaTime
  )
)

lazy val api = project
  .settings(
    name := "api",
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.http4sEmberClient,
      dependencies.http4sEmberServer,
      dependencies.http4sCirce,
      dependencies.http4sDsl,
      dependencies.tapirHttp4s,
      dependencies.tapirJsonCirce,
      dependencies.tapirSwagger
    )
  )
  .aggregate(crawler, library)
  .dependsOn(crawler, library)

lazy val library = project
  .configs(IntegrationTest)
  .settings(
    name := "library",
    testFrameworks ++= commonTestFrameworks,
    Defaults.itSettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.doobie,
      dependencies.doobieHikari,
      dependencies.sqliteJDB
    )
  )

val Http4sVersion = "0.23.16"
val TapirVersion = "1.1.3"
val DoobieVersion = "1.0.0-RC2"

lazy val dependencies =
  new {
    val catsEffect = "org.typelevel" %% "cats-effect" % "3.3.14"
    val scalaScraper = "net.ruippeixotog" %% "scala-scraper" % "3.0.0"
    val scalaTime = "com.github.nscala-time" %% "nscala-time" % "2.32.0"
    val woof = "org.legogroup" %% "woof-core" % "0.4.7"
    val newType = ("io.estatico" %% "newtype" % "0.4.4")
      .cross(CrossVersion.for3Use2_13)
    val http4sEmberServer =
      "org.http4s" %% "http4s-ember-server" % Http4sVersion
    val http4sEmberClient =
      "org.http4s" %% "http4s-ember-client" % Http4sVersion
    val http4sCirce = "org.http4s" %% "http4s-circe" % Http4sVersion
    val http4sDsl = "org.http4s" %% "http4s-dsl" % Http4sVersion
    val tapirHttp4s =
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % TapirVersion
    val tapirSwagger =
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % TapirVersion
    val tapirJsonCirce =
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % TapirVersion
    val slf4j = "org.slf4j" % "slf4j-simple" % "1.7.36"
    val doobie = "org.tpolecat" %% "doobie-core" % DoobieVersion
    val doobieHikari = "org.tpolecat" %% "doobie-hikari" % DoobieVersion
    val sqliteJDB = "org.xerial" % "sqlite-jdbc" % "3.40.0.0"
    val munit = "org.scalameta" %% "munit" % "0.7.29" % Test
    val munitCatsEffect =
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test
    val weaver = "com.disneystreaming" %% "weaver-cats" % "0.8.1"
    val ciris = "is.cir" %% "ciris" % "3.0.0"
  }

lazy val allTestFrameworks =
  new {
    val weaverCatsEffect = new TestFramework("weaver.framework.CatsEffect")
  }

lazy val commonDependencies = Seq(
  dependencies.catsEffect,
  dependencies.slf4j,
  dependencies.woof,
  dependencies.munit,
  dependencies.munitCatsEffect,
  dependencies.weaver,
  dependencies.newType
)

lazy val commonTestFrameworks = Seq(allTestFrameworks.weaverCatsEffect)

enablePlugins(JavaAppPackaging)

addCommandAlias(
  "codeCoverage",
  "coverage ; test ; coverageReport"
)
