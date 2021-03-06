// See 'book of doobie', chapter 06:
// https://tpolecat.github.io/doobie/docs/06-Checking.html
//
package book._06checking

import doobie.implicits._
import hutil.stringformat._

object TypeCheckingQueries extends App {

  import cats.effect.unsafe.implicits.global
  import book.transactor._ // imports Transactor xa + implicit ContextShift cs

  dash80.green.println()

  val y = xa.yolo
  import y._

  s"$dash10 Checking a Query (check prints errors) $dash10".magenta.println()

  case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

  def biggerThan(minPop: Short) =
    sql"""|
          |select code, name, population, gnp, indepyear
          |from country
          |where population > $minPop
          |""".stripMargin.query[Country]

  // Now let’s try the check method provided by YOLO and see what happens.

  biggerThan(0).check.unsafeRunSync()

  s"$dash10 Checking a Query (check for column 4 OK) $dash10".magenta.println()

  case class Country2(code: String, name: String, pop: Int, gnp: Option[BigDecimal])

  def biggerThan2(minPop: Short) =
    sql"""|
          |select code, name, population, gnp
          |from country
          |where population > $minPop
          |""".stripMargin.query[Country2]

  // Now let’s try the check method provided by YOLO and see what happens.

  biggerThan2(0).check.unsafeRunSync()

  s"$dash10 Working Around Bad Metadata (using checkOutput) $dash10".magenta.println()

  biggerThan2(0).checkOutput.unsafeRunSync()

  dash80.green.println()
}
