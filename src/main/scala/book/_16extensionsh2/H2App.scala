// See 'book of doobie', chapter 16:
// https://tpolecat.github.io/doobie/docs/16-Extensions-H2.html
//
package book._16extensionsh2

import cats.effect.{ExitCode, IO, IOApp, Resource}
import doobie._
import doobie.h2._
import doobie.implicits._
import hutil.stringformat._

object H2App extends IOApp {

  // Resource yielding a transactor configured with a bounded connect EC and an unbounded
  // transaction EC. Everything will be closed and shut down cleanly after use.
  val h2Transactor: Resource[IO, H2Transactor[IO]] =
    for {
      ec <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      // blocker <- Blocker[IO]                               // our blocking EC
      xa <- H2Transactor.newH2Transactor[IO](
              "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", // connect URL
              "sa",                                 // username
              "",                                   // password
              ec                                    // await connection here
              //  blocker                               // execute JDBC operations here
            )
    } yield xa

  def run(args: List[String]): IO[ExitCode] =
    h2Transactor.use { xa =>
      // Construct and run your server here!
      for {
        _ <- IO.println(dash80.green)
        n <- sql"select 42".query[Int].unique.transact(xa)
        _ <- IO.println(n)
        _ <- IO.println(dash80.green)
      } yield ExitCode.Success
    }
}
