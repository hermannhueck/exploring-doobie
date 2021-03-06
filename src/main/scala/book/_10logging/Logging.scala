// See 'book of doobie', chapter 10:
// https://tpolecat.github.io/doobie/docs/10-Logging.html
//
package book._10logging

import doobie._
import doobie.implicits._
import hutil.stringformat._

object Logging extends App {

  import cats.effect.unsafe.implicits.global
  import book.transactor._ // imports Transactor xa + implicit ContextShift cs

  dash80.green.println()

  s"$dash10 Basic Statement Logging $dash10".magenta.println()

  def byName(pat: String) = {
    sql"select name, code from country where name like $pat"
      .queryWithLogHandler[(String, String)](LogHandler.jdkLogHandler)
      .to[List]
      .transact(xa)
  }

  s"$dash10 Log Output $dash10".green.println()
  val res0                = byName("U%").unsafeRunSync()
  // res0: List[(String, String)] = List(
  //   ("United Arab Emirates", "ARE"),
  //   ("United Kingdom", "GBR"),
  //   ("Uganda", "UGA"),
  //   ("Ukraine", "UKR"),
  //   ("Uruguay", "URY"),
  //   ("Uzbekistan", "UZB"),
  //   ("United States", "USA"),
  //   ("United States Minor Outlying Islands", "UMI")
  // )
  s"$dash10 Output $dash10".green.println()
  res0 foreach println

  s"$dash10 Implicit Logging $dash10".magenta.println()

  implicit val han = LogHandler.jdkLogHandler

  def byName2(pat: String) = {
    sql"select name, code from country where name like $pat"
      .query[(String, String)] // handler will be picked up here
      .to[List]
      .transact(xa)
  }

  s"$dash10 Log Output $dash10".green.println()
  val res1                 = byName("U%").unsafeRunSync()
  s"$dash10 Output $dash10".green.println()
  res1 foreach println

  s"$dash10 Writing Your Own LogHandler $dash10".magenta.println()

  // case class LogHandler(unsafeRun: LogEvent => Unit)

  // LogEvent has three constructors, all of which provide the SQL string and argument list.

  // Success indicates successful execution and result processing, and provides timing information for both.
  // ExecFailure indicates that query execution failed, due to a key violation for example. This constructor provides timing information only for the (failed) execution as well as the raised exception.
  // ProcessingFailure indicates that execution was successful but resultset processing failed. This constructor provides timing information for both execution and (failed) processing, as well as the raised exception.

  val nop = LogHandler(_ => ())

  val trivial = LogHandler(e => Console.println("*** " + e))
  // trivial: LogHandler = LogHandler(<function1>)
  sql"select 42"
    .queryWithLogHandler[Int](trivial)
    .unique
    .transact(xa)
    .unsafeRunSync()
  // *** Success(select 42,List(),480259 nanoseconds,124281 nanoseconds)
  // res1: Int = 42

  dash80.green.println()
}
