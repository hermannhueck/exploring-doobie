package book._13unittesting

// The doobie-specs2 add-on provides a mix-in trait that we can add to a Specification
// to allow for typechecking of queries, interpreted as a set of specifications.

// Our unit test needs to extend AnalysisSpec and must define a Transactor[IO].
// To construct a testcase for a query, pass it to the check method.
// Note that query arguments are never used, so they can be any values that typecheck.

import org.specs2.mutable.Specification

class AnalysisTestWithSpecs2 extends Specification with doobie.specs2.IOChecker {

  override val transactor = book.transactor.xa

  import UnitTesting._

  check(trivial)
  check(biggerThan(0))
  check(update("", ""))
}
