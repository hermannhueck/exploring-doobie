package book

import cats.effect.IO
import doobie.util.transactor.Transactor

object transactor {

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql:world", // connect URL (driver-specific)
    "postgres",              // user
    ""                       // password
  )
}
