package book._13unittesting

// The doobie-specs2 add-on provides a mix-in trait that we can add to a Specification
// to allow for typechecking of queries, interpreted as a set of specifications.

// Our unit test needs to extend AnalysisSpec and must define a Transactor[IO].
// To construct a testcase for a query, pass it to the check method.
// Note that query arguments are never used, so they can be any values that typecheck.

import munit._

class AnalysisTestSuiteWithMUnit extends FunSuite with doobie.munit.IOChecker {

  override val transactor = book.transactor.xa

  import UnitTesting._

  test("trivial") {
    check(trivial)
  }
  test("biggerThan") {
    check(biggerThan(0))
  }
  test("update") {
    check(update("", ""))
  }
}
