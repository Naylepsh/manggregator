val scala3Version = "3.2.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "manggregator",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.3.14",
    libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "3.0.0",
    libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.32.0",
    libraryDependencies += "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test,
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test
  )

addCommandAlias(
  "codeCoverage",
  "coverage ; test ; coverageReport"
)
