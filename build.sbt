import Dependencies._

ThisBuild / scalaVersion := "3.2.0"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / name := "manggregator"
ThisBuild / organization := "io.naylepsh"
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "manggregator",
    libraryDependencies ++= commonLibraries ++ Seq(
      Libraries.ciris,
      Libraries.slf4j
    )
  )
  .aggregate(crawler, library, api)
  .dependsOn(crawler, library, api)

lazy val crawler = project
  .configs(IntegrationTest)
  .settings(
    name := "crawler",
    testFrameworks ++= commonTestFrameworks,
    Defaults.itSettings,
    libraryDependencies ++= commonLibraries ++ Seq(
      Libraries.scalaScraper,
      Libraries.scalaTime,
      Libraries.sttp,
      Libraries.sttpCats,
      Libraries.sttpCirce,
      Libraries.circe,
      Libraries.circeGeneric,
      Libraries.circeParser
    )
  )

lazy val api = project
  .settings(
    name := "api",
    libraryDependencies ++= commonLibraries ++ Seq(
      Libraries.http4sEmberClient,
      Libraries.http4sEmberServer,
      Libraries.http4sCirce,
      Libraries.http4sDsl,
      Libraries.tapirHttp4s,
      Libraries.tapirJsonCirce,
      Libraries.tapirSwagger
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
    libraryDependencies ++= commonLibraries ++ Seq(
      Libraries.doobie,
      Libraries.doobieHikari,
      Libraries.sqliteJDB
    )
  )

lazy val allTestFrameworks =
  new {
    val weaverCatsEffect = new TestFramework("weaver.framework.CatsEffect")
  }

lazy val commonLibraries = Seq(
  Libraries.catsEffect,
  Libraries.slf4j,
  Libraries.woof,
  Libraries.munit,
  Libraries.munitCatsEffect,
  Libraries.weaver,
  Libraries.newType
)

lazy val commonTestFrameworks = Seq(allTestFrameworks.weaverCatsEffect)

enablePlugins(JavaAppPackaging)

addCommandAlias(
  "codeCoverage",
  "coverage ; test ; coverageReport"
)

addCommandAlias(
  "organizeImports",
  "scalafixEnable ; scalafix OrganizeImports"
)
