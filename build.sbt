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

lazy val dependencies =
  new {
    val catsEffect = "org.typelevel" %% "cats-effect" % "3.3.14"
    val scalaScraper = "net.ruippeixotog" %% "scala-scraper" % "3.0.0"
    val scalaTime = "com.github.nscala-time" %% "nscala-time" % "2.32.0"
    val munit = "org.scalameta" %% "munit" % "0.7.29" % Test
    val munitCatsEffect =
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test
  }

lazy val commonDependencies = Seq(
  dependencies.catsEffect,
  dependencies.munit,
  dependencies.munitCatsEffect
)

addCommandAlias(
  "codeCoverage",
  "coverage ; test ; coverageReport"
)
