import sbt._

object Dependencies {

  import Versions._

  lazy val doobieCore      = "org.tpolecat" %% "doobie-core"      % doobieVersion
  lazy val doobieH2        = "org.tpolecat" %% "doobie-h2"        % doobieVersion
  lazy val doobiePostgres  = "org.tpolecat" %% "doobie-postgres"  % doobieVersion
  lazy val doobieHikari    = "org.tpolecat" %% "doobie-hikari"    % doobieVersion
  lazy val newtype         = "io.estatico"  %% "newtype"          % newTypeVersion
  lazy val circeCore       = "io.circe"     %% "circe-core"       % circeVersion
  lazy val circeParser     = "io.circe"     %% "circe-parser"     % circeVersion
  // lazy val munit           = "org.scalameta"  %% "munit"            % munitVersion
  // lazy val scalaTest       = "org.scalatest"  %% "scalatest"        % scalaTestVersion
  // lazy val scalaCheck      = "org.scalacheck" %% "scalacheck"       % scalaCheckVersion
  lazy val doobieSpecs2    = "org.tpolecat" %% "doobie-specs2"    % doobieVersion
  lazy val doobieScalatest = "org.tpolecat" %% "doobie-scalatest" % doobieVersion
  lazy val doobieMunit     = "org.tpolecat" %% "doobie-munit"     % doobieVersion

  // https://github.com/typelevel/kind-projector
  lazy val kindProjectorPlugin    = compilerPlugin(
    compilerPlugin("org.typelevel" % "kind-projector" % kindProjectorVersion cross CrossVersion.full)
  )
  // https://github.com/oleg-py/better-monadic-for
  lazy val betterMonadicForPlugin = compilerPlugin(
    compilerPlugin("com.olegpy" %% "better-monadic-for" % betterMonadicForVersion)
  )
}
