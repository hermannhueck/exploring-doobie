// See 'book of doobie', chapter 03:
// https://tpolecat.github.io/doobie/docs/03-Connecting.html
//
package book._03connecting

import scala.util.chaining._

import cats.effect.IO
import cats.syntax.applicative._
import doobie._
import doobie.implicits._
import hutil.stringformat._

object ConnectingToADatabase extends App {

  import cats.effect.unsafe.implicits.global
  import book.transactor._

  dash80.green.println()

  // ---------- 1st Program ------------------------------
  s"$dash10 Our First Program $dash10".magenta.println()

  val program1: ConnectionIO[Int] = 42.pure[ConnectionIO]
  // program1: ConnectionIO[Int] = Pure(42)

  val io1: IO[Int] = program1.transact(xa)
  // io1: IO[Int] = Uncancelable(
  //   body = cats.effect.IO$$$Lambda$12397/0x000000084304d840@2c65f453,
  //   event = cats.effect.tracing.TracingEvent$StackTrace
  // )
  io1.unsafeRunSync() pipe println
  // res0: Int = 42

  // ---------- 2nd Program ------------------------------
  s"$dash10 Our Second Program $dash10".magenta.println()

  val program2: ConnectionIO[Int] = sql"select 42".query[Int].unique
  // program2: ConnectionIO[Int] = Suspend(
  //   a = Uncancelable(
  //     body = cats.effect.kernel.MonadCancel$$Lambda$12438/0x0000000843082840@4c3198ad
  //   )
  // )
  val io2: IO[Int]                = program2.transact(xa)
  // io2: IO[Int] = Uncancelable(
  //   body = cats.effect.IO$$$Lambda$12397/0x000000084304d840@c88f399,
  //   event = cats.effect.tracing.TracingEvent$StackTrace
  // )
  io2.unsafeRunSync() pipe println
  // res1: Int = 42

  // ---------- 3rd (monadic) Program ------------------------------
  s"$dash10 Our Third Program (monadic) $dash10".magenta.println()

  val program3: ConnectionIO[(Int, Double)] =
    for {
      a <- sql"select 42".query[Int].unique
      b <- sql"select random()".query[Double].unique
    } yield (a, b)

  program3.transact(xa).unsafeRunSync() pipe println
  // res2: (Int, Double) = (42, 0.011002501472830772)

  // ---------- 3rd a (applicative) Program ------------------------------
  s"$dash10 Our Program 3a (applicative) $dash10".magenta.println()

  import cats.syntax.apply._

  val program3a: ConnectionIO[(Int, Double)] = {
    val a: ConnectionIO[Int]    = sql"select 42".query[Int].unique
    val b: ConnectionIO[Double] = sql"select random()".query[Double].unique
    (a, b).tupled
  }

  program3a.transact(xa).unsafeRunSync() pipe println
  // res3: (Int, Double) = (42, 0.7195786754600704)

  // ---------- 3rd a (applicative) Program ------------------------------
  s"$dash10 Our Program 3b (compose more) $dash10".magenta.println()

  val valuesList: ConnectionIO[List[(Int, Double)]] = program3a.replicateA(5)
  // valuesList: ConnectionIO[List[(Int, Double)]] = FlatMapped(
  //   c = FlatMapped(
  //     c = FlatMapped(
  //       c = FlatMapped(
  //         c = FlatMapped(
  //           c = Suspend(
  //             a = Uncancelable(
  //               body = cats.effect.kernel.MonadCancel$$Lambda$12438/0x0000000843082840@243f864f
  //             )
  //           ),
  //           f = cats.FlatMap$$Lambda$12508/0x00000008430dc040@7b73a984
  //         ),
  //         f = cats.Monad$$Lambda$12386/0x0000000843034840@7ac20abb
  //       ),
  //       f = cats.FlatMap$$Lambda$12561/0x000000084311a040@619a94d0
  //     ),
  //     f = cats.Monad$$Lambda$12386/0x0000000843034840@39cf7b52
  //   ),
  //   f = cats.Monad$$Lambda$12386/0x0000000843034840@5f96da66
  // )
  val result: IO[List[(Int, Double)]]               = valuesList.transact(xa)
  // result: IO[List[(Int, Double)]] = Uncancelable(
  //   body = cats.effect.IO$$$Lambda$12397/0x000000084304d840@2b19e3eb,
  //   event = cats.effect.tracing.TracingEvent$StackTrace
  // )
  result.unsafeRunSync().foreach(println)
  // (42,0.19134460762143135)
  // (42,0.6406009765341878)
  // (42,0.22629678901284933)
  // (42,0.932811641599983)
  // (42,0.7562076565809548)

  s"$dash10 Diving Deeper $dash10".magenta.println()

  import cats.syntax.flatMap._

  val interpreter = KleisliInterpreter[IO].ConnectionInterpreter
  // interpreter: ConnectionOp ~> Kleisli[IO, Connection, γ$9$] = doobie.free.KleisliInterpreter$$anon$11@c8016d5

  val kleisli = program1.foldMap(interpreter)
  // kleisli: Kleisli[IO, Connection, Int] = Kleisli(
  //   cats.data.KleisliFlatMap$$Lambda$8207/142389822@2823704f
  // )

  val io3 = IO(null: java.sql.Connection) >>= kleisli.run // scalafix:ok DisableSyntax.null
  // io3: IO[Int] = Bind(
  //   Delay(<function0>),
  //   cats.data.KleisliFlatMap$$Lambda$8207/142389822@2823704f
  // )

  io3.unsafeRunSync() pipe println // sneaky; program1 never looks at the connection
  // res5: Int = 42

  // s"$dash10 Using Your Own Target Monad (monix.eval.Task instead of cats.effect.IO) $dash10".magenta.println()

  // import monix.eval.Task
  // import monix.execution.Scheduler.Implicits.global

  // val mxa = Transactor.fromDriverManager[Task](
  //   "org.postgresql.Driver",
  //   "jdbc:postgresql:world",
  //   "postgres",
  //   ""
  // )

  // sql"select 42"
  //   .query[Int]
  //   .unique
  //   .transact(mxa)
  //   .runSyncUnsafe()
  //   .pipe(println)

  dash80.green.println()
}
