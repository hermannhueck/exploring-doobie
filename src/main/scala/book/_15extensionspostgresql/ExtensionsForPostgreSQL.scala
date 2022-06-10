// See 'book of doobie', chapter 15:
// https://tpolecat.github.io/doobie/docs/15-Extensions-PostgreSQL.html
//
package book._15extensionspostgresql

import scala.util.chaining._

import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import hutil.stringformat._

object ExtensionsForPostgreSQL extends App {

  import cats.effect.unsafe.implicits.global
  import book.transactor._ // imports Transactor xa + implicit ContextShift cs

  dash80.green.println()

  s"$dash10 Java 8 Time Types (JSR310) $dash10".magenta.println()

  s"$dash10 Array Types $dash10".magenta.println()

  s"$dash10 Enum Types $dash10".magenta.println()

  // PostgreSQL enum type: create type myenum as enum ('foo', 'bar')

  object MyEnum extends Enumeration {
    val foo, bar = Value
  }

  implicit val MyEnumMeta = pgEnum(MyEnum, "myenum")

  sql"select 'foo'::myenum".query[MyEnum.Value].unique.transact(xa).unsafeRunSync() pipe println
  // res0: MyEnum.Value = foo

  // This is Java code
  // public enum MyJavaEnum { foo, bar; }

  // implicit val MyJavaEnumMeta = pgJavaEnum[MyJavaEnum]("myenum")

  sealed trait FooBar

  object FooBar {

    case object Foo extends FooBar
    case object Bar extends FooBar

    def toEnum(e: FooBar): String =
      e match {
        case Foo => "foo"
        case Bar => "bar"
      }

    def fromEnum(s: String): Option[FooBar] =
      Option(s) collect {
        case "foo" => Foo
        case "bar" => Bar
      }

  }

  implicit val FoobarMeta: Meta[FooBar] =
    pgEnumStringOpt("myenum", FooBar.fromEnum, FooBar.toEnum)

  sql"select 'foo'::myenum".query[FooBar].unique.transact(xa).unsafeRunSync() pipe println
  // res1: FooBar = Foo

  s"$dash10 Geometric Types $dash10".magenta.println()

  s"$dash10 PostGIS Types $dash10".magenta.println()

  // libraryDependencies += "net.postgis" % "postgis-jdbc" % "2.3.0"

  // Not provided via doobie.postgres.imports._; you must import them explicitly.
  // import doobie.postgres.pgisimplicits._

  // - PGgeometry
  // - PGbox2d
  // - PGbox3d

  // In addition to the general types above, doobie provides mappings for the following abstract
  // and concrete fine-grained types carried by PGgeometry:

  // - Geometry
  // - ComposedGeom
  // - GeometryCollection
  // - MultiLineString
  // - MultiPolygon
  // - PointComposedGeom
  // - LineString
  // - MultiPoint
  // - Polygon
  // - Point

  s"$dash10 Other Nonstandard Types $dash10".magenta.println()

  // - The uuid schema type is supported and maps to java.util.UUID.
  // - The inet schema type is supported and maps to java.net.InetAddress.
  // - The hstore schema type is supported and maps to both java.util.Map[String, String] and Scala Map[String, String].

  s"$dash10 Extended Error Handling $dash10".magenta.println()

  import cats.syntax.applicative._      // pure
  import cats.syntax.applicativeError._ // attempt

  """val p = sql"oops".query[String].unique // this won't work""".yellow.println()
  val p = sql"oops".query[String].unique // this won't work
  println()

  """p.attempt""".yellow.println()
  p.attempt
    .transact(xa)
    .unsafeRunSync() // attempt is provided by ApplicativeError instance
    .pipe(println)
  // res2: Either[Throwable, String] = Left(
  //   org.postgresql.util.PSQLException: ERROR: syntax error at or near "oops"
  //   Position: 1
  // ) // attempt is provided by ApplicativeError instance

  """p.attemptSqlState""".yellow.println()
  p.attemptSqlState
    .transact(xa)
    .unsafeRunSync() // this catches only SQL exceptions
    .pipe(println)
  // res3: Either[SqlState, String] = Left(SqlState("42601")) // this catches only SQL exceptions

  """p.attemptSomeSqlState { case SqlState("42601") => "caught!" }""".yellow.println()
  p.attemptSomeSqlState { case SqlState("42601") => "caught!" }
    .transact(xa)
    .unsafeRunSync() // catch it
    .pipe(println)
  // res4: Either[String, String] = Left("caught!") // catch it

  """p.attemptSomeSqlState { case sqlstate.class42.SYNTAX_ERROR => "caught!" }""".yellow.println()
  p.attemptSomeSqlState { case sqlstate.class42.SYNTAX_ERROR => "caught!" }
    .transact(xa)
    .unsafeRunSync() // same, w/constant
    .pipe(println)
  // res5: Either[String, String] = Left("caught!") // same, w/constant

  """p.exceptSomeSqlState { case sqlstate.class42.SYNTAX_ERROR => "caught!".pure[ConnectionIO] }""".yellow.println()
  p.exceptSomeSqlState { case sqlstate.class42.SYNTAX_ERROR => "caught!".pure[ConnectionIO] }
    .transact(xa)
    .unsafeRunSync() // recover
    .pipe(println)
  // res6: String = "caught!" // recover

  """p.onSyntaxError("caught!".pure[ConnectionIO])""".yellow.println()
  p.onSyntaxError("caught!".pure[ConnectionIO])
    .transact(xa)
    .unsafeRunSync() // using recovery combinator
    .pipe(println)
  // res7: String = "caught!"

  s"$dash10 Server-Side Statements $dash10".magenta.println()

  s"$dash10 LISTEN and NOTIFY $dash10".magenta.println()

  s"$dash10 Large Objects $dash10".magenta.println()

  s"$dash10 Copy Manager $dash10".magenta.println()

  val q = """
  copy country (name, code, population)
  to stdout (
    encoding 'utf-8',
    force_quote *,
    format csv
  )
  """.stripMargin

  val prog: ConnectionIO[Long] =
    PHC.pgGetCopyAPI(PFCM.copyOut(q, Console.out)) // return value is the row count

  prog.transact(xa).unsafeRunSync() pipe println

  s"$dash10 Copy Manager (Batch Inserts) $dash10".magenta.println()

  import cats.syntax.functor._ // void

  // First a temp table for our experiment.

  val create: ConnectionIO[Unit] =
    sql"""
    CREATE TEMPORARY TABLE food (
      name       VARCHAR,
      vegetarian BOOLEAN,
      calories   INTEGER
    )
  """.update.run.void

  // And some values to insert. Text instances are provided for all the data types we are using here.

  case class Food(name: String, isVegetarian: Boolean, caloriesPerServing: Int)

  val foods = List(
    Food("banana", true, 110),
    Food("cheddar cheese", true, 113),
    Food("Big Mac", false, 1120)
  )

  import cats.Foldable         // Foldable
  import cats.instances.list._ // Foldable[List]

  def insert[F[_]: Foldable](fa: F[Food]): ConnectionIO[Long] =
    sql"COPY food (name, vegetarian, calories) FROM STDIN".copyIn(fa)

  def insert2(fa: List[Food]): ConnectionIO[Long] =
    sql"COPY food (name, vegetarian, calories) FROM STDIN".copyIn(fa)

  import cats.syntax.apply._ // *>

  (create *> insert(foods)).transact(xa).unsafeRunSync() pipe println
  (create *> insert2(foods)).transact(xa).unsafeRunSync() pipe println
  // res8: Long = 3L

  s"$dash10 Fastpath $dash10".magenta.println()

  s"$dash10 EXPLAIN/EXPLAIN ANALYZE $dash10".magenta.println()

  "explain:".yellow.println()
  sql"select name from country"
    .query[String] // Query0[String]
    .explain
    .transact(xa)
    .unsafeRunSync()
    .foreach(println)
  // Seq Scan on country  (cost=0.00..7.39 rows=239 width=11)

  "explainAnalyze:".yellow.println()
  sql"select name from country"
    .query[String] // Query0[String]
    .explainAnalyze
    .transact(xa)
    .unsafeRunSync()
    .foreach(println)
  // Seq Scan on country  (cost=0.00..7.39 rows=239 width=11) (actual time=0.014..1.926 rows=239 loops=1)
  // Planning Time: 0.223 ms
  // Execution Time: 3.974 ms

  dash80.green.println()
}
