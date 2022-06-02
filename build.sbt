import Versions._
import Dependencies._
import ScalacOptions._

val projectName        = "exploring-doobie"
val projectDescription = "Exploring Doobie for functional database access"

ThisBuild / fork                   := true
ThisBuild / turbo                  := true // default: false
ThisBuild / includePluginResolvers := true // default: false
Global / onChangedBuildSource      := ReloadOnSourceChanges

inThisBuild(
  Seq(
    version                  := projectVersion,
    scalaVersion             := scala2Version,
    publish / skip           := true,
    scalacOptions ++= defaultScalacOptions,
    semanticdbEnabled        := true,
    semanticdbVersion        := scalafixSemanticdb.revision,
    scalafixDependencies ++= Seq("com.github.liancheng" %% "organize-imports" % scalafixOrganizeImportsVersion),
    Test / parallelExecution := false,
    // run 100 tests for each property // -s = -minSuccessfulTests
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaCheck, "-s", "100"),
    initialCommands          :=
      s"""|
          |import scala.util.chaining._
          |import scala.concurrent.duration._
          |println()
          |""".stripMargin // initialize REPL
  )
)

lazy val root = (project in file("."))
  .settings(
    name                              := projectName,
    description                       := projectDescription,
    Compile / console / scalacOptions := consoleScalacOptions,
    libraryDependencies ++= Seq(
      doobieCore,
      doobieH2,
      doobiePostgres,
      doobieHikari,
      newtype,
      circeCore,
      circeParser,
      kindProjectorPlugin,
      betterMonadicForPlugin
    ) ++ Seq(
      doobieSpecs2,
      doobieScalatest,
      doobieMunit
    ) // .map(_ % Test)
  )
