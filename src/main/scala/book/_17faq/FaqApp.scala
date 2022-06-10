// See 'book of doobie', chapter 16:
// https://tpolecat.github.io/doobie/docs/17-FAQ.html
//
package book._17faq

import cats.data._
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import hutil.stringformat._
import shapeless._

object FaqApp extends App {

  dash80.green.println()

  import cats.effect.unsafe.implicits.global
  import book.transactor.xa

  s"$dash10 How do I do an IN clause? $dash10".magenta.println()

  """
  This used to be very irritating, but as of 0.4.0 there is a good solution.
  See the section on IN clauses in Chapter 5 and Chapter 8 on statement fragments.""".stripMargin.println()

  s"$dash10 How do I ascribe an SQL type to an interpolated parameter? $dash10".magenta.println()

  """
  Interpolated parameters are replaced with ? placeholders, so if you need to ascribe an SQL type
  you can use vendor-specific syntax in conjunction with the interpolated value. For example, in PostgreSQL you use :: type:
  """.stripMargin.println()

  """
  {
    val y = xa.yolo
    import y._
    val s = "foo"
    sql"select $s".query[String].check.unsafeRunSync()
    sql"select $s :: char".query[String].check.unsafeRunSync()
  }
  """.stripMargin.println()

  {
    val y = xa.yolo
    import y._
    val s = "foo"
    sql"select $s".query[String].check.unsafeRunSync()
    sql"select $s :: char".query[String].check.unsafeRunSync()
  }

  s"$dash10 How do I do several things in the same transaction? $dash10".magenta.println()

  """
  You can use a for comprehension to compose any number of ConnectionIO programs, and then call .transact(xa) on the result.
  All of the composed programs will run in the same transaction. For this reason it’s useful for your API
  to expose values in ConnectionIO, so higher-level code can place transaction boundaries as needed."""
    .stripMargin
    .println()

  s"$dash10 How do I run something outside of a transaction? $dash10".magenta.println()

  """
  Transactor.transact takes a ConnectionIO and constructs an IO or similar that will run it in a single transaction,
  but it is also possible to include transaction boundaries within a ConnectionIO, and to disable transaction handling
  altogether. Some kinds of DDL statements may require this for some databases.
  You can define a combinator to do this for you.
  """.stripMargin.println()

  """
  /** Take a program `p` and return an equivalent one that first commits any ongoing transaction, runs `p` without
    * transaction handling, then starts a new transaction.
    */
  def withoutTransaction[A](p: ConnectionIO[A]): ConnectionIO[A] =
    // FC is an alias for connection
    FC.setAutoCommit(true).bracket(_ => p)(_ => FC.setAutoCommit(false))
  """.stripMargin.println()

  """
  Note that you need both of these operations if you are using a Transactor
  because it will always start a transaction and will try to commit on completion.""".stripMargin.println()

  s"$dash10 How do I turn an arbitrary SQL string into a Query0/Update0? $dash10".magenta.println()

  """
  As of doobie 0.4.0 this is done via statement fragments. Here we choose the sort order dynamically."""
    .stripMargin
    .println()

  """
  case class Code(country: String)
  case class City(code: Code, name: String, population: Int)

  def cities(code: Code, asc: Boolean): Query0[City] = {
    val ord = if (asc) fr"ASC" else fr"DESC"
    val sql = fr\"\"\"
    SELECT countrycode, name, population
    FROM   city
    WHERE  countrycode = $code
    ORDER BY name\"\"\" ++ ord
    sql.query[City]
  }
  """.stripMargin.println()

  case class Code(country: String)
  case class City(code: Code, name: String, population: Int)

  def cities(code: Code, asc: Boolean): Query0[City] = {
    val ord = if (asc) fr"ASC" else fr"DESC"
    val sql = fr"""
    SELECT countrycode, name, population
    FROM   city
    WHERE  countrycode = $code
    ORDER BY name""" ++ ord
    sql.query[City]
  }

  """
  We can check the resulting Query0 as expected.

  {
    val y = xa.yolo
    import y._
    cities(Code("USA"), true).check.unsafeRunSync()
  }

  And it works!
  """.stripMargin.println()

  {
    val y = xa.yolo
    import y._
    cities(Code("USA"), true).check.unsafeRunSync()
  }

  """

  {
    val y = xa.yolo
    import y._
    cities(Code("USA"), true).stream.take(5).quick.unsafeRunSync()
    IO.println("---").unsafeRunSync()
    cities(Code("USA"), false).stream.take(5).quick.unsafeRunSync()
  }
  """.stripMargin.println()

  {
    val y = xa.yolo
    import y._
    cities(Code("USA"), true).stream.take(5).quick.unsafeRunSync()
    IO.println("---").unsafeRunSync()
    cities(Code("USA"), false).stream.take(5).quick.unsafeRunSync()
  }

  s"$dash10 How do I handle outer joins? $dash10".magenta.println()

  """
  With an outer join you end up with set of nullable columns, which you typically want to map to a single Option
  of some composite type, which doobie can do for you. If all columns are null you will get back None.
  """.stripMargin.println()

  """
  case class Country(name: String, code: String)
  case class City2(name: String, district: String)

  val join =
    sql\"\"\"
    select c.name, c.code,
           k.name, k.district
    from country c
    left outer join city k
    on c.capital = k.id
  \"\"\".query[(Country, Option[City2])]
  """.stripMargin.println()

  case class Country(name: String, code: String)
  case class City2(name: String, district: String)

  val join =
    sql"""
    select c.name, c.code,
           k.name, k.district
    from country c
    left outer join city k
    on c.capital = k.id
  """.query[(Country, Option[City2])]

  """
  Some examples, filtered for size.
  """.stripMargin.println()

  """
  {
    val y = xa.yolo
    import y._
    join.stream.filter(_._1.name.startsWith("United")).quick.unsafeRunSync()
  }
  """.stripMargin.println()

  {
    val y = xa.yolo
    import y._
    join.stream.filter(_._1.name.startsWith("United")).quick.unsafeRunSync()
  }

  s"$dash10 How do I log the SQL produced for my query after interpolation? $dash10".magenta.println()

  """
  As of doobie 0.4 there is a reasonable solution to the logging/instrumentation question. See Chapter 10 for more details.
  """.stripMargin.println()

  s"$dash10 Why is there no Get or Put for SQLXML? $dash10".magenta.println()

  """
  There are a lot of ways to handle SQLXML so there is no pre-defined strategy, but here is one that maps scala.xml.Elem to SQLXML via streaming.
  """.stripMargin.println()

  """
  import doobie.enum.JdbcType.Other
  import java.sql.SQLXML
  import scala.xml.{Elem, XML}

  implicit val XmlMeta: Meta[Elem] =
    Meta
      .Advanced
      .one[Elem](
        Other,
        NonEmptyList.of("xml"),
        (rs, n) => XML.load(rs.getObject(n).asInstanceOf[SQLXML].getBinaryStream),
        (ps, n, e) => {
          val sqlXml = ps.getConnection.createSQLXML
          val osw    = new java.io.OutputStreamWriter(sqlXml.setBinaryStream)
          XML.write(osw, e, "UTF-8", false, null)
          osw.close
          ps.setObject(n, sqlXml)
        },
        (_, _, _) => sys.error("update not supported, sorry")
      )
  """.stripMargin.println()

  import doobie.enumerated.JdbcType
  import java.sql.SQLXML
  import scala.xml.{Elem, XML}

  implicit val XmlMeta: Meta[Elem] =
    Meta
      .Advanced
      .one[Elem](
        JdbcType.Other,
        NonEmptyList.of("xml"),
        (rs, n) => XML.load(rs.getObject(n).asInstanceOf[SQLXML].getBinaryStream),
        (ps, n, e) => {
          val sqlXml = ps.getConnection.createSQLXML
          val osw    = new java.io.OutputStreamWriter(sqlXml.setBinaryStream)
          XML.write(osw, e, "UTF-8", false, null)
          osw.close
          ps.setObject(n, sqlXml)
        },
        (_, _, _) => sys.error("update not supported, sorry")
      )

  s"$dash10 How do I set the chunk size for streaming results? $dash10".magenta.println()

  """
  By default streams constructed with the sql interpolator are fetched Query.DefaultChunkSize rows at a time (currently 512).
  If you wish to change this chunk size you can use streamWithChunkSize for queries, and withGeneratedKeysWithChunkSize for updates that return results.
  """.stripMargin.println()

  s"$dash10 My Postgres domains are all type checking as DISTINCT! How can I get my Yolo tests to pass? $dash10"
    .magenta
    .println()

  """

  Domains with check constraints will type check as DISTINCT. For Doobie later than 0.4.4, in order to get the type checks to pass, you can define a Meta of with target type Distinct and xmap that instances. For example,

  import cats.data.NonEmptyList
  import doobie._
  import doobie.enum.JdbcType

  object distinct {

    def string(name: String): Meta[String] =
      Meta.Advanced.many(
        NonEmptyList.of(JdbcType.Distinct, JdbcType.VarChar),
        NonEmptyList.of(name),
        _ getString _,
        _.setString(_, _),
        _.updateString(_, _)
      )
  }

  case class NonEmptyString(value: String)

  // If the domain for NonEmptyStrings is nes
  implicit val nesMeta: Meta[NonEmptyString] = {
    distinct.string("nes").imap(NonEmptyString.apply)(_.value)
  }
  """.stripMargin.println()

  s"$dash10 How do I use java.time types with Doobie? $dash10".magenta.println()

  """
  This depends on whether the underlying JDBC driver you’re using supports java.time.* types natively.
  (“native support” means that you can hand the driver e.g. a value of java.time.Instant and it will know
  how to convert that to a value on-the-wire that the actual database can understand)
  """.stripMargin.println()

  """
  If you’re using PostgreSQL, you can import that instances via import doobie.postgres.implicits._
  """.stripMargin.println()

  """
  If your JDBC driver supports the java.time types you’re using natively, use import doobie.implicits.javatimedrivernative._.
  Database driver 	                java.time.Instant 	                java.time.LocalDate
  Postgres (org.postgresql.Driver) 	doobie.postgres.implicits._ 	        doobie.postgres.implicits._
  MySQL (com.mysql.jdbc.Driver) 	        doobie.implicits.legacy.instant._ 	doobie.implicits.legacy.localdate._
  """.stripMargin.println()

  """
  References:
  Postgres JDBC - Using Java 8 Date and Time classes:
  https://jdbc.postgresql.org/documentation/head/8-date-time.html""".stripMargin.println()

  dash80.green.println()
}
