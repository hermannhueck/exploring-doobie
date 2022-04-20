// See 'book of doobie', chapter 04:
// https://tpolecat.github.io/doobie/docs/04-Selecting.html
//
package book._04selecting

import hutil.stringformat._

import cats.effect.IO

import doobie._
import doobie.implicits._

object SelectingData extends App {

  import cats.effect.unsafe.implicits.global
  import book.transactor._

  dash80.green.println()

  s"$dash10 Reading Rows into Collections $dash10".magenta.println()

  sql"select name from country" // Fragment
    .query[String] // Query0[String]
    .to[List]         // ConnectionIO[List[String]]
    .transact(xa)     // IO[List[String]]
    .unsafeRunSync()  // List[String]
    .take(5)          // List[String]
    .foreach(println) // Unit
  // Afghanistan
  // Netherlands
  // Netherlands Antilles
  // Albania
  // Algeria

  s"$dash10 Internal Streaming $dash10".magenta.println()

  sql"select name from country" // Fragment
    .query[String]    // Query0[String]
    .stream           // Stream[ConnectionIO, String]
    .take(5)          // Stream[ConnectionIO, String]
    .compile          // Stream.CompileOps[ConnectionIO, ConnectionIO, String]
    .toList           // ConnectionIO[List[String]]
    .transact(xa)     // IO[List[String]]
    .unsafeRunSync()  // List[String]
    .foreach(println) // Unit
  // Afghanistan
  // Netherlands
  // Netherlands Antilles
  // Albania
  // Algeria

  s"$dash10 With server-side LIMIT $dash10".magenta.println()

  sql"select name from country limit 5"
    .query[String] // Query0[String]
    .to[List]         // ConnectionIO[List[String]]
    .transact(xa)     // IO[List[String]]
    .unsafeRunSync()  // List[String]
    .foreach(println) // Unit
  // Afghanistan
  // Netherlands
  // Netherlands Antilles
  // Albania
  // Algeria

  s"$dash10 YOLO Mode $dash10".magenta.println()

  val y = xa.yolo // a stable reference is required
  import y._

  sql"select name from country" // Fragment
    .query[String] // Query0[String]
    .stream                                       // Stream[ConnectionIO, String]
    .take(5)                                      // Stream[ConnectionIO, String]
    .quick                                        // IO[Unit]
    .unsafeRunSync()
  //   Afghanistan
  //   Netherlands
  //   Netherlands Antilles
  //   Albania
  //   Algeria

  s"$dash10 Multi-Column Queries $dash10".magenta.println()

  sql"select code, name, population, gnp from country" // Fragment
    .query[(String, String, Int, Option[Double])] // Query0[(String, String, Int, Option[Double])]
    .stream                                       // fs2.Stream[ConnectionIO[(String, String, Int, Option[Double])]]
    .take(5)                                      // fs2.Stream[ConnectionIO[(String, String, Int, Option[Double])]]
    .quick                                        // IO[Unit]
    .unsafeRunSync()                              // Unit
  //   (AFG,Afghanistan,22720000,Some(5976.0))
  //   (NLD,Netherlands,15864000,Some(371362.0))
  //   (ANT,Netherlands Antilles,217000,Some(1941.0))
  //   (ALB,Albania,3401200,Some(3205.0))
  //   (DZA,Algeria,31471000,Some(49982.0))

  s"$dash10 Shapeless HList Support $dash10".magenta.println()

  import shapeless.{::, HNil}

  type CountryHList = String :: String :: Int :: Option[Double] :: HNil

  sql"select code, name, population, gnp from country" // Fragment
    .query[CountryHList] // Query0[CountryHList]
    .stream              // fs2.Stream[ConnectionIO[CountryHList]]
    .take(5)             // fs2.Stream[ConnectionIO[CountryHList]]
    .quick               // IO[Unit]
    .unsafeRunSync()     // Unit
  //   AFG :: Afghanistan :: 22720000 :: Some(5976.0) :: HNil
  //   NLD :: Netherlands :: 15864000 :: Some(371362.0) :: HNil
  //   ANT :: Netherlands Antilles :: 217000 :: Some(1941.0) :: HNil
  //   ALB :: Albania :: 3401200 :: Some(3205.0) :: HNil
  //   DZA :: Algeria :: 31471000 :: Some(49982.0) :: HNil

  s"$dash10 Shapeless Record Support $dash10".magenta.println()

  import shapeless.record.Record

  type Rec =
    Record.`Symbol("code") -> String, Symbol("name") -> String, Symbol("pop") -> Int, Symbol("gnp") -> Option[Double]`.T

  sql"select code, name, population, gnp from country" // Fragment
    .query[Rec] // Query0[Rec]
    .stream          // fs2.Stream[ConnectionIO[Rec]]
    .take(5)         // fs2.Stream[ConnectionIO[Rec]]
    .quick           // IO[Unit]
    .unsafeRunSync() // Unit
  //   AFG :: Afghanistan :: 22720000 :: Some(5976.0) :: HNil
  //   NLD :: Netherlands :: 15864000 :: Some(371362.0) :: HNil
  //   ANT :: Netherlands Antilles :: 217000 :: Some(1941.0) :: HNil
  //   ALB :: Albania :: 3401200 :: Some(3205.0) :: HNil
  //   DZA :: Algeria :: 31471000 :: Some(49982.0) :: HNil

  s"$dash10 Mapping rows to a case class $dash10".magenta.println()

  case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

  sql"select code, name, population, gnp from country" // Fragment
    .query[Country]  // Query0[Country]
    .stream          // fs2.Stream[ConnectionIO[Country]]
    .take(5)         // fs2.Stream[ConnectionIO[Country]]
    .quick           // IO[Unit]
    .unsafeRunSync() // Unit
  //   Country(AFG,Afghanistan,22720000,Some(5976.0))
  //   Country(NLD,Netherlands,15864000,Some(371362.0))
  //   Country(ANT,Netherlands Antilles,217000,Some(1941.0))
  //   Country(ALB,Albania,3401200,Some(3205.0))
  //   Country(DZA,Algeria,31471000,Some(49982.0))

  s"$dash10 Nested case classes and Tuples (HLists, Records) $dash10".magenta.println()

  case class Code(code: String)
  case class Country2(name: String, pop: Int, gnp: Option[Double])

  sql"select code, name, population, gnp from country" // Fragment
    .query[(Code, Country2)] // Query0[(Code, Country2)]
    .stream                  // fs2.Stream[ConnectionIO[(Code, Country2)]]
    .take(5)                 // fs2.Stream[ConnectionIO[(Code, Country2)]]
    .quick                   // IO[Unit]
    .unsafeRunSync()         // Unit
  //   (Code(AFG),Country2(Afghanistan,22720000,Some(5976.0)))
  //   (Code(NLD),Country2(Netherlands,15864000,Some(371362.0)))
  //   (Code(ANT),Country2(Netherlands Antilles,217000,Some(1941.0)))
  //   (Code(ALB),Country2(Albania,3401200,Some(3205.0)))
  //   (Code(DZA),Country2(Algeria,31471000,Some(49982.0)))

  s"$dash10 Creating a Map from a List of Pairs $dash10".magenta.println()

  sql"select code, name, population, gnp from country" // Fragment
    .query[(Code, Country2)] // Query0[(Code, Country2)]
    .stream                  // fs2.Stream[ConnectionIO[(Code, Country2)]]
    .take(5)                 // fs2.Stream[ConnectionIO[(Code, Country2)]]
    .compile                 // IO[Map[Code, Country2]]
    .toList                  // List[(Code, Country2)]
    .map(_.toMap)            // Map[Code, Country2]
    .quick                   // IO[Unit]
    .unsafeRunSync()         // Unit
  //   HashMap(Code(ANT) -> Country2(Netherlands Antilles,217000,Some(1941.0)), Code(ALB) -> Country2(Albania,3401200,Some(3205.0)), Code(DZA) -> Country2(Algeria,31471000,Some(49982.0)), Code(NLD) -> Country2(Netherlands,15864000,Some(371362.0)), Code(AFG) -> Country2(Afghanistan,22720000,Some(5976.0)))
  import fs2.Stream

  s"$dash10 Final Streaming $dash10".magenta.println()

  val s: Stream[IO, Country2] =
    sql"select name, population, gnp from country" // Fragment
      .query[Country2] // Query0[Country2]
      .stream       // Stream[ConnectionIO, Country2]
      .transact(xa) // Stream[IO, Country2]
  // s: Stream[IO, Country2] = Stream(..)

  s.take(5)
    .compile
    .toVector
    .unsafeRunSync()
    .foreach(println)
  // Country2(Afghanistan,22720000,Some(5976.0))
  // Country2(Netherlands,15864000,Some(371362.0))
  // Country2(Netherlands Antilles,217000,Some(1941.0))
  // Country2(Albania,3401200,Some(3205.0))
  // Country2(Algeria,31471000,Some(49982.0))

  s"$dash10 Diving Deeper $dash10".magenta.println()

  import cats.syntax.applicative._ // for pure

  val proc = HC.stream[(Code, Country2)](
    "select code, name, population, gnp from country", // statement
    ().pure[PreparedStatementIO],                      // prep (none)
    512                                                // chunk size
  )
  // proc: Stream[ConnectionIO, (Code, Country2)] = Stream(..)

  proc
    .take(5)      // Stream[ConnectionIO, (Code, Country2)]
    .compile
    .toList       // ConnectionIO[List[(Code, Country2)]]
    .map(_.toMap) // ConnectionIO[Map[Code, Country2]]
    .quick
    .unsafeRunSync()
  //   HashMap(Code(ANT) -> Country2(Netherlands Antilles,217000,Some(1941.0)), Code(ALB) -> Country2(Albania,3401200,Some(3205.0)), Code(DZA) -> Country2(Algeria,31471000,Some(49982.0)), Code(NLD) -> Country2(Netherlands,15864000,Some(371362.0)), Code(AFG) -> Country2(Afghanistan,22720000,Some(5976.0)))

  dash80.green.println()
}
